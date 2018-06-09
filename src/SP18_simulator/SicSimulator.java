package SP18_simulator;

import java.io.File;
import java.util.*;

/**
 * �ùķ����ͷμ��� �۾��� ����Ѵ�. VisualSimulator���� ������� ��û�� ������ �̿� ����
 * ResourceManager�� �����Ͽ� �۾��� �����Ѵ�.  
 * 
 * �ۼ����� ���ǻ��� : <br>
 *  1) ���ο� Ŭ����, ���ο� ����, ���ο� �Լ� ������ �󸶵��� ����. ��, ������ ������ �Լ����� �����ϰų� ������ ��ü�ϴ� ���� ������ ��.<br>
 *  2) �ʿ信 ���� ����ó��, �������̽� �Ǵ� ��� ��� ���� ����.<br>
 *  3) ��� void Ÿ���� ���ϰ��� ������ �ʿ信 ���� �ٸ� ���� Ÿ������ ���� ����.<br>
 *  4) ����, �Ǵ� �ܼ�â�� �ѱ��� ��½�Ű�� �� ��. (ä������ ����. �ּ��� ���Ե� �ѱ��� ��� ����)<br>
 * 
 * <br><br>
 *  + �����ϴ� ���α׷� ������ ��������� �����ϰ� ���� �е��� ������ ��� �޺κп� ÷�� �ٶ��ϴ�. ���뿡 ���� �������� ���� �� �ֽ��ϴ�.
 */
public class SicSimulator {
	ResourceManager rMgr;
	char[] currentInst;
	
	List<String> logList = new ArrayList<>();
	
	public SicSimulator(ResourceManager resourceManager) {
		// �ʿ��ϴٸ� �ʱ�ȭ ���� �߰�
		this.rMgr = resourceManager;
	}

	/**
	 * ��������, �޸� �ʱ�ȭ �� ���α׷� load�� ���õ� �۾� ����.
	 * ��, object code�� �޸� ���� �� �ؼ��� SicLoader���� �����ϵ��� �Ѵ�. 
	 */
	public void load(File program) {
		/* �޸� �ʱ�ȭ, �������� �ʱ�ȭ ��*/
		rMgr.initializeResource();
	}

	/**
	 * 1���� instruction�� ����� ����� ���δ�. 
	 */
	public void oneStep() {
		char [] upperByte = rMgr.getMemory(rMgr.register[8], 2);
		int temp = (upperByte[0] >>> 4) + (upperByte[0] & 15);
		int opcode = temp;
		boolean extForm = false;
		boolean pcRelative = false;
		boolean usedXregister = false;
		boolean immediate = false;
		boolean indirect = false;
		int address = 0;
		
		if((temp & 2) == 2)
		{
			opcode -= 2;
			indirect = true;
		}
		
		if((temp & 1) == 1)
		{
			opcode -= 1;
			immediate = true;
		}
		
		temp = (upperByte[1] >>> 8);
		extForm = (temp & 1) == 1;
		pcRelative = (temp & 2) == 2;
		usedXregister = (temp & 8) == 8;
		
		switch(opcode)
		{
			case 0x14:
				logList.add("STL");
				rMgr.register[8] += 3;
				if(extForm)
				{
					rMgr.register[8] += 1;
					logList.set(logList.size()-1, "+" + logList.get(logList.size()-1));
				}
				
				break;
				
			case 0x48:
				logList.add("JSUB");
				rMgr.register[8] += 3;
				if(extForm)
				{
					rMgr.register[8] += 1;
					logList.set(logList.size()-1, "+" + logList.get(logList.size()-1));
				}
				
				break;
				
			case 0x00:
				logList.add("LDA");
				rMgr.register[8] += 3;
				if(extForm)
				{
					rMgr.register[8] += 1;
					logList.set(logList.size()-1, "+" + logList.get(logList.size()-1));
				}
				
				break;
				
			case 0x28:
				logList.add("COMP");
				rMgr.register[8] += 3;
				if(extForm)
				{
					rMgr.register[8] += 1;
					logList.set(logList.size()-1, "+" + logList.get(logList.size()-1));
				}
				
				break;
		
			case 0x4c:
				logList.add("RSUB");
				rMgr.register[8] += 3;
				if(extForm)
				{
					rMgr.register[8] += 1;
					logList.set(logList.size()-1, "+" + logList.get(logList.size()-1));
				}
				
				break;
			
			case 0x50:
				logList.add("LDCH");
				rMgr.register[8] += 3;
				if(extForm)
				{
					rMgr.register[8] += 1;
					logList.set(logList.size()-1, "+" + logList.get(logList.size()-1));
				}
				
				break;
			
			case 0xdc:
				logList.add("WD");
				rMgr.register[8] += 3;
				if(extForm)
				{
					rMgr.register[8] += 1;
					logList.set(logList.size()-1, "+" + logList.get(logList.size()-1));
				}
				
				break;
				
			case 0x3c:
				logList.add("J");
				rMgr.register[8] += 3;
				if(extForm)
				{
					rMgr.register[8] += 1;
					logList.set(logList.size()-1, "+" + logList.get(logList.size()-1));
				}
				
				break;
			
			case 0x0c:
				logList.add("STA");
				rMgr.register[8] += 3;
				if(extForm)
				{
					rMgr.register[8] += 1;
					logList.set(logList.size()-1, "+" + logList.get(logList.size()-1));
				}
				
				break;
				
			case 0xb4:
				logList.add("CLEAR");
				rMgr.register[8] += 2;
				
				break;
			
			case 0x74:
				logList.add("LDT");
				rMgr.register[8] += 3;
				if(extForm)
				{
					rMgr.register[8] += 1;
					logList.set(logList.size()-1, "+" + logList.get(logList.size()-1));
				}
				
				break;
			
			case 0xe0:
				logList.add("TD");
				rMgr.register[8] += 3;
				if(extForm)
				{
					rMgr.register[8] += 1;
					logList.set(logList.size()-1, "+" + logList.get(logList.size()-1));
				}
				
				break;
			
			case 0xd8:
				logList.add("RD");
				rMgr.register[8] += 3;
				if(extForm)
				{
					rMgr.register[8] += 1;
					logList.set(logList.size()-1, "+" + logList.get(logList.size()-1));
				}
				
				break;
				
			case 0xa0:
				logList.add("COMPR");
				rMgr.register[8] += 2;
				
				break;
			
			case 0x54:
				logList.add("STCH");
				rMgr.register[8] += 3;
				if(extForm)
				{
					rMgr.register[8] += 1;
					logList.set(logList.size()-1, "+" + logList.get(logList.size()-1));
				}
				
				break;
			
			case 0xb8:
				logList.add("TIXR");
				rMgr.register[8] += 2;
				
				break;
			
			case 0x38:
				logList.add("JLT");
				rMgr.register[8] += 3;
				if(extForm)
				{
					rMgr.register[8] += 1;
					logList.set(logList.size()-1, "+" + logList.get(logList.size()-1));
				}
				
				break;
			
			case 0x10:
				logList.add("STX");
				rMgr.register[8] += 3;
				if(extForm)
				{
					rMgr.register[8] += 1;
					logList.set(logList.size()-1, "+" + logList.get(logList.size()-1));
				}
				
				break;
				
			case 0x30:
				logList.add("JEQ");
				rMgr.register[8] += 3;
				if(extForm)
				{
					rMgr.register[8] += 1;
					logList.set(logList.size()-1, "+" + logList.get(logList.size()-1));
				}
				
				break;
		}
		
		/*
		char [] xbpe = rMgr.getMemory(rMgr.register[8]+1, 2);
		int format = 3;
		if((xbpe[0] & 1) == 1)
		{
			format = 4;
		}
		
		System.out.println(rMgr.getMemory(rMgr.register[8], format));
		rMgr.register[8] += format/2;
		*/
		System.out.println("PC: " +rMgr.register[8]);
		for(int i = 0; i < logList.size(); i++)
		{
			System.out.print(logList.get(i) + " ");
		}
		System.out.println();
	}
	
	/**
	 * ���� ��� instruction�� ����� ����� ���δ�.
	 */
	public void allStep() {
	}
	
	/**
	 * �� �ܰ踦 ������ �� ���� ���õ� ����� ���⵵�� �Ѵ�.
	 */
	public void addLog(String log) {
	}	
}
