package SP18_simulator;

import java.io.*;

/**
 * SicLoader�� ���α׷��� �ؼ��ؼ� �޸𸮿� �ø��� ������ �����Ѵ�. �� �������� linker�� ���� ���� �����Ѵ�. <br>
 * <br>
 * SicLoader�� �����ϴ� ���� ���� ��� ������ ����.<br>
 * - program code�� �޸𸮿� �����Ű��<br>
 * - �־��� ������ŭ �޸𸮿� �� ���� �Ҵ��ϱ�<br>
 * - �������� �߻��ϴ� symbol, ���α׷� �����ּ�, control section �� ������ ���� ���� ���� �� ����
 */
public class SicLoader
{
	ResourceManager rMgr;

	// ���� �ε��ϴ� ��Ʈ�� ������ ǥ���Ѵ�.
	private int currentSection;

	public SicLoader(ResourceManager resourceManager)
	{
		// �ʿ��ϴٸ� �ʱ�ȭ
		setResourceManager(resourceManager);
		currentSection = 0;
	}

	/**
	 * Loader�� ���α׷��� ������ �޸𸮸� �����Ų��.
	 * 
	 * @param rMgr
	 */
	public void setResourceManager(ResourceManager resourceManager)
	{
		this.rMgr = resourceManager;
	}

	/**
	 * object code�� �о load������ �����Ѵ�. load�� �����ʹ� resourceManager�� �����ϴ� �޸𸮿� �ö󰡵��� �Ѵ�.
	 * load�������� ������� symbol table �� �ڷᱸ�� ���� resourceManager�� �����Ѵ�.
	 * @param objectCode �о���� ����
	 */
	public void load(File objectCode){
		try
		{
			// ���ڷ� ���� �̸��� ������ ���� ������Ʈ �ڵ带 �о� ���δ�.
			FileReader fileReader = new FileReader(objectCode);
			BufferedReader bufReader = new BufferedReader(fileReader);
			String line = "";
			
			// ������ ���� ������ ������ �� �پ� �о� ���δ�.
			while((line = bufReader.readLine()) != null)
			{
				// �о���� ���ڿ��� ����ִٸ� ���� ������ �д´�.
				if(line.length() == 0)
					continue;
				
				// ���ڿ��� ó�� ���ڷ� �� �ִ� ���ڵ� ǥ�÷� ó���� �޸��Ѵ�.
				switch(line.charAt(0))
				{
					// Header Record�� ���,
					// �ش� ���� ���α׷��� �̸��� �����ּ�, ���α׷� ����, ���α׷��� �޸𸮻��� �����ּ� ���� �����Ѵ�.
					case 'H':
						int progNameLength = line.length()-13;
						String programName = line.substring(1, progNameLength);
						
						rMgr.setProgName(programName, currentSection);
						rMgr.setProgStartAddr(line.substring(progNameLength+1, progNameLength+7), currentSection);
						rMgr.setProgLength(line.substring(line.length()-6, line.length()), currentSection);
						
						rMgr.symtabList.putSymbol(programName, rMgr.getProgStartAddr(currentSection));
						break;
					
					// Define Record�� ���,
					// �ش� symbol�� �� �ּҸ� ���̺� �����Ѵ�.
					case 'D':
						int symNameLength = 0;
						int symNameStart = 1;
						
						for(int i = 1; i < line.length(); i++)
						{
							if(line.charAt(i) == '0')
							{
								String symbol = line.substring(symNameStart, symNameStart+symNameLength);
								int address = Integer.parseInt(line.substring(symNameStart+symNameLength, symNameStart+symNameLength+6), 16);
								
								rMgr.symtabList.putSymbol(symbol, address);
								symNameStart += symNameLength + 6;
								i += 5;
								symNameLength = 0;
								continue;
							}
							symNameLength++;
						}
						break;
					
					// Refer Record�� ���, �Ѿ��.
					case 'R':
						break;
					
					// Text Record�� ���,
					// �����ּҺ��� ��õ� ���̸�ŭ �޸𸮿� ������Ʈ �ڵ带 �ε��Ѵ�.
					// ������Ʈ �ڵ带 �ε��ϱ� ��, �� char���� �� ���ڰ� ��⵵�� packing ������ ��ģ��.
					case 'T':
						int currentAddr = Integer.parseInt(line.substring(1, 7), 16) + rMgr.getProgStartAddr(currentSection);
						int codeLength = Integer.parseInt(line.substring(7, 9), 16);
						char[] packedOpcode = packing(line.substring(9, line.length()).toCharArray());
						
						rMgr.setMemory(currentAddr, packedOpcode, codeLength);
						break;
					
					// Modification Record�� ���,
					// ������ ���� EXTAB�� ������ �ּ�, ������ �κ��� ����, �ּҸ� ���� ������ �� ������, �ɺ� ������ �����Ѵ�.
					case 'M':
						int modifLocation = Integer.parseInt(line.substring(1, 7), 16) + +rMgr.getProgStartAddr(currentSection);
						int modifSize = Integer.parseInt(line.substring(7, 9), 16);
						char modifMode = line.charAt(9);
						String symbol = line.substring(10, line.length());
						
						rMgr.extabList.putExSymbol(symbol, modifLocation, modifSize, modifMode, currentSection);
						break;
					
					// End Record�� ���,
					// ��Ʈ�� ������ ǥ���ϴ� currentSection�� ���� �ø���.
					case 'E':
						currentSection++;
						break;

				}
			}
			
			// EXTAB�� ����Ǿ��ִ� ���� �������� ������
			// �޸𸮿� �ö��ִ� ��ɾ �����Ѵ�.
			for(int i = 0; i < rMgr.extabList.size(); i++)
			{
				String symbol = rMgr.extabList.getSymbol(i);
				int modifSize = rMgr.extabList.getModifSize(i);
				char modifMode = rMgr.extabList.getModifMode(i);
				
				String modifAddr = "000000";
				if(modifSize == 5)
					modifAddr = String.format("%05X", rMgr.symtabList.search(symbol));
				else if(modifSize == 6)
					modifAddr = String.format("%06X", rMgr.symtabList.search(symbol));
				char[] packedAddr = packing(modifAddr.toCharArray());
				
				rMgr.modifMemory(rMgr.extabList.getaddress(i), packedAddr, packedAddr.length, modifMode);
			}
			
			bufReader.close();
		}
		catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e)
		{
			
		}
		
	};

	// �ܼ� ���� ������ �����͸� �� char�� �ȿ� �� ���� ���� �޼ҵ�
	// ���� ��� 1,7�� �ִٸ�,
	// �ϳ��� char���� 0000 0001 0000 0007�� �ǵ��� ����
	private char[] packing(char[] inputData)
	{
		int length = (inputData.length / 2) + (inputData.length % 2);
		char[] outputData = new char[length];

		int upByte = 0;
		int downByte = 0;

		if (inputData.length % 2 == 0)
		{

			for (int i = 0; i < length; i++)
			{
				upByte = inputData[i * 2] - '0';
				downByte = inputData[i * 2 + 1] - '0';
				if (upByte >= 10)
					upByte -= 7;
				if (downByte >= 10)
					downByte -= 7;

				outputData[i] = (char) ((upByte << 8) + downByte);
			}
		}
		else
		{
			downByte = (inputData[0] - '0');
			if(downByte >= 10)
				downByte -= 7;
			outputData[0] = (char) downByte;
			
			for (int i = 1; i < length; i++)
			{
				upByte = inputData[i * 2 - 1] - '0';
				downByte = inputData[i * 2] - '0';
				if (upByte >= 10)
					upByte -= 7;
				if (downByte >= 10)
					downByte -= 7;

				outputData[i] = (char) ((upByte << 8) + downByte);
			}
		}
		return outputData;
	}
}
