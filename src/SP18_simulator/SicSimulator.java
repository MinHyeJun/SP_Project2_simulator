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
	public static final int A_REGISTER = 0;
	public static final int X_REGISTER = 1;
	public static final int L_REGISTER = 2;
	public static final int B_REGISTER = 3;
	public static final int S_REGISTER = 4;
	public static final int T_REGISTER = 5;
	public static final int F_REGISTER = 6;
	public static final int PC_REGISTER = 8;
	public static final int SW_REGISTER = 9;
	
	ResourceManager rMgr;
	char[] currentInst;
	
	List<String> instList = new ArrayList<>();
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
		char [] bytes = rMgr.getMemory(rMgr.getRegister(PC_REGISTER), 2);
		int temp = (bytes[0] >>> 4) + (bytes[0] & 15);
		int opcode = temp;
		boolean extForm = false;
		boolean pcRelative = false;
		boolean usedXregister = false;
		boolean immediate = false;
		boolean indirect = false;
		int address = 0;
		int registerNum = 0;
		int difference = 0;
		char [] instruction;
		
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
		
		temp = (bytes[1] >>> 8);
		extForm = (temp & 1) == 1;
		pcRelative = (temp & 2) == 2;
		usedXregister = (temp & 8) == 8;
		
		switch(opcode)
		{
			case 0x14:  // STL ��ɾ�: L �������� ���� �ش� �ּҿ� �����ϴ� ��ɾ�
				logList.add("STL");
				if(extForm)
				{
					logList.set(logList.size()-1, "+" + logList.get(logList.size()-1));
					instruction = rMgr.getMemory(rMgr.getRegister(PC_REGISTER), 4);
					address = ((instruction[1] & 15) << 16) + ((instruction[2] >>> 8) << 12) + ((instruction[2] & 15) << 8) + ((instruction[3] >>> 8) << 4) + (instruction[3] & 15);
					rMgr.setRegister(PC_REGISTER, rMgr.getRegister(PC_REGISTER) + 4);
					
					rMgr.modifMemory(address + (3 - rMgr.intToChar(rMgr.getRegister(L_REGISTER)).length), rMgr.intToChar(rMgr.getRegister(L_REGISTER)), rMgr.intToChar(rMgr.getRegister(L_REGISTER)).length, '+');
				}
				else
				{
					instruction = rMgr.getMemory(rMgr.getRegister(PC_REGISTER), 3);
					address = ((instruction[1] & 15) << 8) + ((instruction[2] >>> 8) << 4) + (instruction[2] & 15);
					rMgr.setRegister(PC_REGISTER, rMgr.getRegister(PC_REGISTER) + 3);
					
					if(pcRelative)
						address += rMgr.getRegister(PC_REGISTER);
					rMgr.modifMemory(address + (3 - rMgr.intToChar(rMgr.getRegister(L_REGISTER)).length), rMgr.intToChar(rMgr.getRegister(L_REGISTER)), rMgr.intToChar(rMgr.getRegister(L_REGISTER)).length, '+');
				}
				
				break;
				
			case 0x48: // JSUB ��ɾ�: �ּҰ����� ���� ������ �̵�
				logList.add("JSUB");
				if(extForm)
				{
					logList.set(logList.size()-1, "+" + logList.get(logList.size()-1));
					instruction = rMgr.getMemory(rMgr.getRegister(PC_REGISTER), 4);
					address = ((instruction[1] & 15) << 16) + ((instruction[2] >> 8) << 12) + ((instruction[2] & 15) << 8) + ((instruction[3] >> 8) << 4) + (instruction[3] & 15);
					rMgr.setRegister(L_REGISTER, rMgr.getRegister(PC_REGISTER) + 4);
					
					rMgr.setRegister(PC_REGISTER, address);
				}
				else
				{
					instruction = rMgr.getMemory(rMgr.getRegister(PC_REGISTER), 3);
					address = ((instruction[1] & 15) << 8) + ((instruction[2] >> 8) << 4) + (instruction[2] & 15);
					rMgr.setRegister(L_REGISTER, rMgr.getRegister(PC_REGISTER) + 3);
					
					if(pcRelative)
						address += rMgr.getRegister(PC_REGISTER);
					rMgr.setRegister(PC_REGISTER, address);
				}
				break;
				
			case 0x00:  ////////////////////////////////////////////////�׽�Ʈ �ʿ�
				logList.add("LDA");
				if(extForm)
				{
					logList.set(logList.size()-1, "+" + logList.get(logList.size()-1));
					instruction = rMgr.getMemory(rMgr.getRegister(PC_REGISTER), 4);
					address = ((instruction[1] & 15) << 16) + ((instruction[2] >> 8) << 12) + ((instruction[2] & 15) << 8) + ((instruction[3] >> 8) << 4) + (instruction[3] & 15);
					rMgr.setRegister(PC_REGISTER, rMgr.getRegister(PC_REGISTER) + 4);

					char[] data = rMgr.getMemory(address, 3);
					rMgr.setRegister(A_REGISTER, rMgr.byteToInt(data));				
				}
				else
				{
					instruction = rMgr.getMemory(rMgr.getRegister(PC_REGISTER), 3);
					address = ((instruction[1] & 15) << 8) + ((instruction[2] >> 8) << 4) + (instruction[2] & 15);
					rMgr.setRegister(PC_REGISTER, rMgr.getRegister(PC_REGISTER) + 3);

					if(pcRelative)
						address += rMgr.getRegister(PC_REGISTER);
					char[] data = rMgr.getMemory(address, 3);
					rMgr.setRegister(A_REGISTER, rMgr.byteToInt(data));
				}
				break;
				
			case 0x28: // COMP ��ɾ�: A�������� ���� ��ɾ �־��� ���� ���Ѵ�.
				logList.add("COMP");
				if(extForm)
				{
					logList.set(logList.size()-1, "+" + logList.get(logList.size()-1));
					instruction = rMgr.getMemory(rMgr.getRegister(PC_REGISTER), 4);
					address = ((instruction[1] & 15) << 16) + ((instruction[2] >>> 8) << 12) + ((instruction[2] & 15) << 8) + ((instruction[3] >>> 8) << 4) + (instruction[3] & 15);
					rMgr.setRegister(PC_REGISTER, rMgr.getRegister(PC_REGISTER) + 4);
					
					if(immediate)
					{
						difference = rMgr.getRegister(A_REGISTER) - address;
						rMgr.setRegister(SW_REGISTER, difference);
						System.out.println("SW: " + rMgr.getRegister(SW_REGISTER));
					}

				}
				else
				{
					instruction = rMgr.getMemory(rMgr.getRegister(PC_REGISTER), 3);
					address = ((instruction[1] & 15) << 8) + ((instruction[2] >>> 8) << 4) + (instruction[2] & 15);
					rMgr.setRegister(PC_REGISTER, rMgr.getRegister(PC_REGISTER) + 3);

					if(immediate)
					{
						difference = rMgr.getRegister(A_REGISTER) - address;
						rMgr.setRegister(SW_REGISTER, difference);
						System.out.println("SW: " + rMgr.getRegister(SW_REGISTER));
					}
				}
				break;
		
			case 0x4c: // RSUB ��ɾ�: L �������Ϳ� ����Ǿ��ִ� �ּҷ� �̵�(ȣ�� ���� ���� ��ɾ�� ���ư�)
				logList.add("RSUB");
				rMgr.setRegister(PC_REGISTER, rMgr.getRegister(L_REGISTER));
				break;
			
			case 0x50:  // LDCH ��ɾ�: �ش� �ּ��� ���� A�������� ���� 1����Ʈ�� �ҷ��´�.
				logList.add("LDCH");
				if(extForm)
				{
					logList.set(logList.size()-1, "+" + logList.get(logList.size()-1));
					instruction = rMgr.getMemory(rMgr.getRegister(PC_REGISTER), 4);
					address = ((instruction[1] & 15) << 16) + ((instruction[2] >>> 8) << 12) + ((instruction[2] & 15) << 8) + ((instruction[3] >>> 8) << 4) + (instruction[3] & 15);
					rMgr.setRegister(PC_REGISTER, rMgr.getRegister(PC_REGISTER) + 4);
					
					char[] data = rMgr.getMemory(address + rMgr.getRegister(X_REGISTER), 1);
					rMgr.setRegister(A_REGISTER, rMgr.byteToInt(data));
					System.out.println((char)rMgr.getRegister(A_REGISTER));
					
				}
				else
				{
					instruction = rMgr.getMemory(rMgr.getRegister(PC_REGISTER), 3);
					address = ((instruction[1] & 15) << 8) + ((instruction[2] >>> 8) << 4) + (instruction[2] & 15);
					rMgr.setRegister(PC_REGISTER, rMgr.getRegister(PC_REGISTER) + 3);
					
					if(pcRelative)
						address += rMgr.getRegister(PC_REGISTER);
					char[] data = rMgr.getMemory(address + rMgr.getRegister(X_REGISTER), 1);
					rMgr.setRegister(A_REGISTER, rMgr.byteToInt(data));
					System.out.println((char)rMgr.getRegister(A_REGISTER));
				}
				
				break;
			
			case 0xdc: // WD ��ɾ�: ������ ���(�Ǵ� ����)�� A �������� ���� 1����Ʈ�� ���� ����Ѵ�.
				logList.add("WD");
				if(extForm)
				{
					logList.set(logList.size()-1, "+" + logList.get(logList.size()-1));
					instruction = rMgr.getMemory(rMgr.getRegister(PC_REGISTER), 4);
					address = ((instruction[1] & 15) << 16) + ((instruction[2] >>> 8) << 12) + ((instruction[2] & 15) << 8) + ((instruction[3] >>> 8) << 4) + (instruction[3] & 15);
					rMgr.setRegister(PC_REGISTER, rMgr.getRegister(PC_REGISTER) + 4);
					
					char[] deviceInfo = rMgr.getMemory(address, 1);
					String deviceName = String.format("%X%X", deviceInfo[0] >> 8, deviceInfo[0] & 15);
					System.out.println(deviceName);
					rMgr.writeDevice(deviceName);
					System.out.print(Integer.toBinaryString(rMgr.getRegister(A_REGISTER)));
				}
				else
				{
					instruction = rMgr.getMemory(rMgr.getRegister(PC_REGISTER), 3);
					address = ((instruction[1] & 15) << 8) + ((instruction[2] >>> 8) << 4) + (instruction[2] & 15);
					rMgr.setRegister(PC_REGISTER, rMgr.getRegister(PC_REGISTER) + 3);
					
					if(pcRelative)
						address += rMgr.getRegister(PC_REGISTER);
					char[] deviceInfo = rMgr.getMemory(address, 1);
					String deviceName = String.format("%X%X", deviceInfo[0] >> 8, deviceInfo[0] & 15);
					System.out.println(deviceName);
					rMgr.writeDevice(deviceName);
					System.out.print(Integer.toBinaryString(rMgr.getRegister(A_REGISTER)));
				}
				
				break;
				
			case 0x3c: ///////////////////////////////////////////////////////////�׽�Ʈ �ʿ�
				logList.add("J");
				if(extForm)
				{
					logList.set(logList.size()-1, "+" + logList.get(logList.size()-1));
					instruction = rMgr.getMemory(rMgr.getRegister(PC_REGISTER), 4);
					address = ((instruction[1] & 15) << 16) + ((instruction[2] >> 8) << 12) + ((instruction[2] & 15) << 8) + ((instruction[3] >> 8) << 4) + (instruction[3] & 15);
					if((instruction[1] & 15) == 15)
						address += (0xFFF << 20);
					rMgr.setRegister(PC_REGISTER, address);
				}
				else
				{
					instruction = rMgr.getMemory(rMgr.getRegister(PC_REGISTER), 3);
					address = ((instruction[1] & 15) << 8) + ((instruction[2] >> 8) << 4) + (instruction[2] & 15);
					System.out.print(Integer.toBinaryString(address));
					if((instruction[1] & 15) == 15)
						address += (0xFFFFF << 12);
					if(pcRelative)
						address += rMgr.getRegister(PC_REGISTER);
					rMgr.setRegister(PC_REGISTER, address);
				}
				
				break;
			
			case 0x0c:
				logList.add("STA"); ///////////////////////////////////////////////�׽�Ʈ �ʿ�
				if(extForm)
				{
					logList.set(logList.size()-1, "+" + logList.get(logList.size()-1));
					instruction = rMgr.getMemory(rMgr.getRegister(PC_REGISTER), 4);
					address = ((instruction[1] & 15) << 16) + ((instruction[2] >> 8) << 12) + ((instruction[2] & 15) << 8) + ((instruction[3] >> 8) << 4) + (instruction[3] & 15);
					rMgr.setRegister(PC_REGISTER, rMgr.getRegister(PC_REGISTER) + 4);
					
					rMgr.modifMemory(address + (3 - rMgr.intToChar(rMgr.getRegister(A_REGISTER)).length), rMgr.intToChar(rMgr.getRegister(A_REGISTER)), rMgr.intToChar(rMgr.getRegister(A_REGISTER)).length, '+');
				}
				else
				{
					instruction = rMgr.getMemory(rMgr.getRegister(PC_REGISTER), 3);
					address = ((instruction[1] & 15) << 8) + ((instruction[2] >> 8) << 4) + (instruction[2] & 15);
					rMgr.setRegister(PC_REGISTER, rMgr.getRegister(PC_REGISTER) + 3);
					
					if(pcRelative)
						address += rMgr.getRegister(PC_REGISTER);
					rMgr.modifMemory(address + (3 - rMgr.intToChar(rMgr.getRegister(A_REGISTER)).length), rMgr.intToChar(rMgr.getRegister(A_REGISTER)), rMgr.intToChar(rMgr.getRegister(A_REGISTER)).length, '+');
				}
				break;
				
			case 0xb4:  // CLEAR ��ɾ�: �ش� ���������� ���� 0���� �ʱ�ȭ ��Ű�� ��ɾ�
				logList.add("CLEAR");
				instruction = rMgr.getMemory(rMgr.getRegister(PC_REGISTER), 2);
				rMgr.setRegister(PC_REGISTER, rMgr.getRegister(PC_REGISTER) + 2);
				
				registerNum = instruction[1] >>> 8;
				rMgr.setRegister(registerNum, 0);
				break;
			
			case 0x74:
				logList.add("LDT"); /////////////////////////////////////////// �׽�Ʈ �ʿ�
				if(extForm)
				{
					logList.set(logList.size()-1, "+" + logList.get(logList.size()-1));
					instruction = rMgr.getMemory(rMgr.getRegister(PC_REGISTER), 4);
					address = ((instruction[1] & 15) << 16) + ((instruction[2] >> 8) << 12) + ((instruction[2] & 15) << 8) + ((instruction[3] >> 8) << 4) + (instruction[3] & 15);
					rMgr.setRegister(PC_REGISTER, rMgr.getRegister(PC_REGISTER) + 4);

					char[] data = rMgr.getMemory(address, 3);
					rMgr.setRegister(T_REGISTER, rMgr.byteToInt(data));				
				}
				else
				{
					instruction = rMgr.getMemory(rMgr.getRegister(PC_REGISTER), 3);
					address = ((instruction[1] & 15) << 8) + ((instruction[2] >> 8) << 4) + (instruction[2] & 15);
					rMgr.setRegister(PC_REGISTER, rMgr.getRegister(PC_REGISTER) + 3);

					if(pcRelative)
						address += rMgr.getRegister(PC_REGISTER);
					char[] data = rMgr.getMemory(address, 3);
					rMgr.setRegister(T_REGISTER, rMgr.byteToInt(data));
				}
				break;
			
			case 0xe0: // TD ��ɾ�: �ش� �̸��� ���(�Ǵ� ����)�� ����� ��Ʈ���� Ȯ���Ѵ�.
				logList.add("TD");
				if(extForm)
				{
					logList.set(logList.size()-1, "+" + logList.get(logList.size()-1));
					instruction = rMgr.getMemory(rMgr.getRegister(PC_REGISTER), 4);
					address = ((instruction[1] & 15) << 16) + ((instruction[2] >> 8) << 12) + ((instruction[2] & 15) << 8) + ((instruction[3] >> 8) << 4) + (instruction[3] & 15);
					rMgr.setRegister(PC_REGISTER, rMgr.getRegister(PC_REGISTER) + 4);
					
					char[] deviceInfo = rMgr.getMemory(address, 1);
					String deviceName = String.format("%X%X", deviceInfo[0] >> 8, deviceInfo[0] & 15);
					System.out.println(deviceName);
					rMgr.testDevice(deviceName);
				}
				else
				{
					instruction = rMgr.getMemory(rMgr.getRegister(PC_REGISTER), 3);
					address = ((instruction[1] & 15) << 8) + ((instruction[2] >>> 8) << 4) + (instruction[2] & 15);
					rMgr.setRegister(PC_REGISTER, rMgr.getRegister(PC_REGISTER) + 3);
					
					if(pcRelative)
						address += rMgr.getRegister(PC_REGISTER);
					char[] deviceInfo = rMgr.getMemory(address, 1);
					String deviceName = String.format("%X%X", deviceInfo[0] >> 8, deviceInfo[0] & 15);
					System.out.println(deviceName);
					rMgr.testDevice(deviceName);
				}
				
				break;
			
			case 0xd8:  // RD ��ɾ�: �ش� ���(�Ǵ� ����)���� ���� �ϳ��� �о� A�������Ϳ� �����Ѵ�.
				logList.add("RD");
				if(extForm)
				{
					logList.set(logList.size()-1, "+" + logList.get(logList.size()-1));
					instruction = rMgr.getMemory(rMgr.getRegister(PC_REGISTER), 4);
					address = ((instruction[1] & 15) << 16) + ((instruction[2] >> 8) << 12) + ((instruction[2] & 15) << 8) + ((instruction[3] >> 8) << 4) + (instruction[3] & 15);
					rMgr.setRegister(PC_REGISTER, rMgr.getRegister(PC_REGISTER) + 4);
					
					char[] deviceInfo = rMgr.getMemory(address, 1);
					String deviceName = String.format("%X%X", deviceInfo[0] >> 8, deviceInfo[0] & 15);
					rMgr.setRegister(A_REGISTER, rMgr.readDevice(deviceName));
					System.out.println((char)rMgr.getRegister(A_REGISTER));
				}
				else
				{
					instruction = rMgr.getMemory(rMgr.getRegister(PC_REGISTER), 3);
					address = ((instruction[1] & 15) << 8) + ((instruction[2] >>> 8) << 4) + (instruction[2] & 15);
					rMgr.setRegister(PC_REGISTER, rMgr.getRegister(PC_REGISTER) + 3);
					
					if(pcRelative)
						address += rMgr.getRegister(PC_REGISTER);
					char[] deviceInfo = rMgr.getMemory(address, 1);
					String deviceName = String.format("%X%X", deviceInfo[0] >> 8, deviceInfo[0] & 15);
					rMgr.setRegister(A_REGISTER, rMgr.readDevice(deviceName));
					System.out.println((char)rMgr.getRegister(A_REGISTER));
				}
				
				break;
				
			case 0xa0:  // COMPR ��ɾ�: �� �������� ���� ���Ѵ�.
				logList.add("COMPR");
				instruction = rMgr.getMemory(rMgr.getRegister(PC_REGISTER), 2);
				rMgr.setRegister(PC_REGISTER, rMgr.getRegister(PC_REGISTER) + 2);
				
				registerNum = instruction[1] >>> 8;
				int compareRegister = instruction[1] & 15;
				difference = rMgr.getRegister(registerNum) - rMgr.getRegister(compareRegister);
				rMgr.setRegister(SW_REGISTER, difference);
				System.out.println("SW: " + rMgr.getRegister(SW_REGISTER));
				break;
			
			case 0x54:  // STCH ��ɾ�: A�������� ���� 1����Ʈ�� ����� ���ڸ� ������ �ּҿ� �����Ѵ�.
				logList.add("STCH");
				if(extForm)
				{
					logList.set(logList.size()-1, "+" + logList.get(logList.size()-1));
					instruction = rMgr.getMemory(rMgr.getRegister(PC_REGISTER), 4);
					address = ((instruction[1] & 15) << 16) + ((instruction[2] >> 8) << 12) + ((instruction[2] & 15) << 8) + ((instruction[3] >> 8) << 4) + (instruction[3] & 15);
					rMgr.setRegister(PC_REGISTER, rMgr.getRegister(PC_REGISTER) + 4);
					
					char [] data = rMgr.intToChar(rMgr.getRegister(A_REGISTER) & 255);
					System.out.println(Integer.toBinaryString(data[0]));
					rMgr.modifMemory(address + rMgr.getRegister(X_REGISTER), data, 1, '+');
				}
				else
				{
					instruction = rMgr.getMemory(rMgr.getRegister(PC_REGISTER), 3);
					address = ((instruction[1] & 15) << 8) + ((instruction[2] >> 8) << 4) + (instruction[2] & 15);
					rMgr.setRegister(PC_REGISTER, rMgr.getRegister(PC_REGISTER) + 3);
					
					if(pcRelative)
						address += rMgr.getRegister(PC_REGISTER);
					char [] data = rMgr.intToChar(rMgr.getRegister(A_REGISTER) & 255);
					rMgr.modifMemory(address + rMgr.getRegister(X_REGISTER), data, 1, '+');
				}
				break;
			
			case 0xb8:  ////////////////////////////////////////////////////////////////�׽�Ʈ �ʿ�
				logList.add("TIXR");
				instruction = rMgr.getMemory(rMgr.getRegister(PC_REGISTER), 2);
				rMgr.setRegister(PC_REGISTER, rMgr.getRegister(PC_REGISTER) + 2);
				
				registerNum = instruction[1] >>> 8;
				rMgr.setRegister(X_REGISTER, rMgr.getRegister(X_REGISTER)+1);
				difference = rMgr.getRegister(X_REGISTER) - rMgr.getRegister(registerNum);
				rMgr.setRegister(SW_REGISTER, difference);
				break;
			
			case 0x38:  // JLT ��ɾ�: �� �� �۴ٸ� ��õ� �ּҷ� �̵��Ѵ�.
				logList.add("JLT");
				if(extForm)
				{
					logList.set(logList.size()-1, "+" + logList.get(logList.size()-1));
					instruction = rMgr.getMemory(rMgr.getRegister(PC_REGISTER), 4);
					address = ((instruction[1] & 15) << 16) + ((instruction[2] >>> 8) << 12) + ((instruction[2] & 15) << 8) + ((instruction[3] >>> 8) << 4) + (instruction[3] & 15);
					rMgr.setRegister(PC_REGISTER, rMgr.getRegister(PC_REGISTER) + 4);
					
					if((instruction[1] & 15) == 15)
						address += (0xFFF << 20);
					
					if(rMgr.getRegister(SW_REGISTER) < 0)
					{
						rMgr.setRegister(PC_REGISTER, address);
					}
				}
				else
				{
					instruction = rMgr.getMemory(rMgr.getRegister(PC_REGISTER), 3);
					address = ((instruction[1] & 15) << 8) + ((instruction[2] >>> 8) << 4) + (instruction[2] & 15);
					rMgr.setRegister(PC_REGISTER, rMgr.getRegister(PC_REGISTER) + 3);
					
					if((instruction[1] & 15) == 15)
							address += (0xFFFFF << 12);
					
					if(pcRelative)
						address += rMgr.getRegister(PC_REGISTER);
					
					if(rMgr.getRegister(SW_REGISTER) < 0)
					{
						rMgr.setRegister(PC_REGISTER, address);
					}
				}
				
				break;
			
			case 0x10:  ///////////////////////////////////////////////�׽�Ʈ �ʿ�
				logList.add("STX");
				if(extForm)
				{
					logList.set(logList.size()-1, "+" + logList.get(logList.size()-1));
					instruction = rMgr.getMemory(rMgr.getRegister(PC_REGISTER), 4);
					address = ((instruction[1] & 15) << 16) + ((instruction[2] >>> 8) << 12) + ((instruction[2] & 15) << 8) + ((instruction[3] >>> 8) << 4) + (instruction[3] & 15);
					rMgr.setRegister(PC_REGISTER, rMgr.getRegister(PC_REGISTER) + 4);
	
					rMgr.modifMemory(address + (3 - rMgr.intToChar(rMgr.getRegister(X_REGISTER)).length), rMgr.intToChar(rMgr.getRegister(X_REGISTER)), rMgr.intToChar(rMgr.getRegister(X_REGISTER)).length, '+');
					System.out.println(Integer.toBinaryString(rMgr.intToChar(rMgr.getRegister(X_REGISTER))[0]));
				}
				else
				{
					instruction = rMgr.getMemory(rMgr.getRegister(PC_REGISTER), 3);
					address = ((instruction[1] & 15) << 8) + ((instruction[2] >>> 8) << 4) + (instruction[2] & 15);
					rMgr.setRegister(PC_REGISTER, rMgr.getRegister(PC_REGISTER) + 3);
					
					if(pcRelative)
						address += rMgr.getRegister(PC_REGISTER);
					rMgr.modifMemory(address + (3 - rMgr.intToChar(rMgr.getRegister(X_REGISTER)).length), rMgr.intToChar(rMgr.getRegister(X_REGISTER)), rMgr.intToChar(rMgr.getRegister(X_REGISTER)).length, '+');
				}
				break;
				
			case 0x30:  ///////////////////////////////////////////////////////////�׽�Ʈ �ʿ�
				logList.add("JEQ");
				if(extForm)
				{
					logList.set(logList.size()-1, "+" + logList.get(logList.size()-1));
					instruction = rMgr.getMemory(rMgr.getRegister(PC_REGISTER), 4);
					address = ((instruction[1] & 15) << 16) + ((instruction[2] >>> 8) << 12) + ((instruction[2] & 15) << 8) + ((instruction[3] >>> 8) << 4) + (instruction[3] & 15);
					rMgr.setRegister(PC_REGISTER, rMgr.getRegister(PC_REGISTER) + 4);
					
					if((instruction[1] & 15) == 15)
						address += (0xFFF << 20);
					
					if(rMgr.getRegister(SW_REGISTER) == 0)
					{
						rMgr.setRegister(PC_REGISTER, address);
					}
				}
				else
				{
					instruction = rMgr.getMemory(rMgr.getRegister(PC_REGISTER), 3);
					address = ((instruction[1] & 15) << 8) + ((instruction[2] >>> 8) << 4) + (instruction[2] & 15);
					rMgr.setRegister(PC_REGISTER, rMgr.getRegister(PC_REGISTER) + 3);
					
					if((instruction[1] & 15) == 15)
						address += (0xFFFFF << 12);
					if(pcRelative)
						address += rMgr.getRegister(PC_REGISTER);
					if(rMgr.getRegister(SW_REGISTER) == 0)
					{
						rMgr.setRegister(PC_REGISTER, address);
					}
				}
				
				break;
		}
		
		System.out.println("PC: " + rMgr.getRegister(PC_REGISTER));
		System.out.println(logList.get(logList.size()-1));
		rMgr.printMemory();
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
