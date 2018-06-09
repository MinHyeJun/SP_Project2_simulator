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
		char [] bytes = rMgr.getMemory(rMgr.getRegister(X_REGISTER), 2);
		int temp = (bytes[0] >>> 4) + (bytes[0] & 15);
		int opcode = temp;
		boolean extForm = false;
		boolean pcRelative = false;
		boolean usedXregister = false;
		boolean immediate = false;
		boolean indirect = false;
		int address = 0;
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
					instruction = rMgr.getMemory(rMgr.getRegister(X_REGISTER), 4);
					rMgr.setRegister(X_REGISTER, rMgr.getRegister(X_REGISTER) + 4);
					address = ((instruction[1] & 15) << 16) + ((instruction[2] >>> 8) << 12) + ((instruction[2] & 15) << 8) + ((instruction[3] >>> 8) << 4) + (instruction[3] & 15);
					rMgr.setMemory(address, rMgr.intToChar(4096), rMgr.intToChar(4096).length);
				}
				else
				{
					instruction = rMgr.getMemory(rMgr.getRegister(X_REGISTER), 3);
					address = ((instruction[1] & 15) << 8) + ((instruction[2] >>> 8) << 4) + (instruction[2] & 15);
					rMgr.setRegister(X_REGISTER, rMgr.getRegister(X_REGISTER) + 3);
					if(pcRelative)
						address += rMgr.getRegister(X_REGISTER);
					rMgr.modifMemory(address + (3 - rMgr.intToChar(rMgr.getRegister(L_REGISTER)).length), rMgr.intToChar(rMgr.getRegister(L_REGISTER)), rMgr.intToChar(rMgr.getRegister(L_REGISTER)).length, '+');
				}
				
				break;
				
			case 0x48: // JSUB ��ɾ�: �ּҰ����� ���� ������ �̵�
				logList.add("JSUB");
				if(extForm)
				{
					instruction = rMgr.getMemory(rMgr.getRegister(X_REGISTER), 4);
					address = ((instruction[1] & 15) << 16) + ((instruction[2] >>> 8) << 12) + ((instruction[2] & 15) << 8) + ((instruction[3] >>> 8) << 4) + (instruction[3] & 15);
					logList.set(logList.size()-1, "+" + logList.get(logList.size()-1));
					rMgr.setRegister(L_REGISTER, rMgr.getRegister(X_REGISTER) + 4);
					rMgr.setRegister(X_REGISTER, address);
				}
				else
				{
					instruction = rMgr.getMemory(rMgr.getRegister(X_REGISTER), 3);
					address = ((instruction[1] & 15) << 8) + ((instruction[2] >>> 8) << 4) + (instruction[2] & 15);
					rMgr.setRegister(L_REGISTER, rMgr.getRegister(X_REGISTER) + 3);
					if(pcRelative)
						address += rMgr.getRegister(X_REGISTER);
					rMgr.setRegister(X_REGISTER, address);
				}
				break;
				
			case 0x00:
				logList.add("LDA");
				if(extForm)
				{
					logList.set(logList.size()-1, "+" + logList.get(logList.size()-1));
					instruction = rMgr.getMemory(rMgr.getRegister(X_REGISTER), 4);
					address = ((instruction[1] & 15) << 16) + ((instruction[2] >>> 8) << 12) + ((instruction[2] & 15) << 8) + ((instruction[3] >>> 8) << 4) + (instruction[3] & 15);
					char[] data = rMgr.getMemory(address, 6);
					
					rMgr.setRegister(X_REGISTER, rMgr.getRegister(X_REGISTER) + 4);
				}
				else
				{
					instruction = rMgr.getMemory(rMgr.getRegister(X_REGISTER), 3);
					address = ((instruction[1] & 15) << 8) + ((instruction[2] >>> 8) << 4) + (instruction[2] & 15);
					
					rMgr.setRegister(X_REGISTER, rMgr.getRegister(X_REGISTER) + 3);
				}
				
				break;
				
			case 0x28:
				logList.add("COMP");
				if(extForm)
				{
					instruction = rMgr.getMemory(rMgr.getRegister(X_REGISTER), 4);
					logList.set(logList.size()-1, "+" + logList.get(logList.size()-1));
					rMgr.setRegister(X_REGISTER, rMgr.getRegister(X_REGISTER) + 4);
				}
				else
				{
					instruction = rMgr.getMemory(rMgr.getRegister(X_REGISTER), 3);
					rMgr.setRegister(X_REGISTER, rMgr.getRegister(X_REGISTER) + 3);
				}
				
				break;
		
			case 0x4c: // RSUB ��ɾ�: L �������Ϳ� ����Ǿ��ִ� �ּҷ� �̵�(ȣ�� ���� ���� ��ɾ�� ���ư�)
				logList.add("RSUB");
				rMgr.setRegister(X_REGISTER, rMgr.getRegister(L_REGISTER));
				break;
			
			case 0x50:
				logList.add("LDCH");
				if(extForm)
				{
					instruction = rMgr.getMemory(rMgr.getRegister(X_REGISTER), 4);
					logList.set(logList.size()-1, "+" + logList.get(logList.size()-1));
					rMgr.setRegister(X_REGISTER, rMgr.getRegister(X_REGISTER) + 4);
				}
				else
				{
					instruction = rMgr.getMemory(rMgr.getRegister(X_REGISTER), 3);
					rMgr.setRegister(X_REGISTER, rMgr.getRegister(X_REGISTER) + 3);
				}
				
				break;
			
			case 0xdc:
				logList.add("WD");
				if(extForm)
				{
					instruction = rMgr.getMemory(rMgr.getRegister(X_REGISTER), 4);
					logList.set(logList.size()-1, "+" + logList.get(logList.size()-1));
					rMgr.setRegister(X_REGISTER, rMgr.getRegister(X_REGISTER) + 4);
				}
				else
				{
					instruction = rMgr.getMemory(rMgr.getRegister(X_REGISTER), 3);
					rMgr.setRegister(X_REGISTER, rMgr.getRegister(X_REGISTER) + 3);
				}
				
				break;
				
			case 0x3c:
				logList.add("J");
				if(extForm)
				{
					instruction = rMgr.getMemory(rMgr.getRegister(X_REGISTER), 4);
					logList.set(logList.size()-1, "+" + logList.get(logList.size()-1));
					rMgr.setRegister(X_REGISTER, rMgr.getRegister(X_REGISTER) + 4);
				}
				else
				{
					instruction = rMgr.getMemory(rMgr.getRegister(X_REGISTER), 3);
					rMgr.setRegister(X_REGISTER, rMgr.getRegister(X_REGISTER) + 3);
				}
				
				break;
			
			case 0x0c:
				logList.add("STA");
				if(extForm)
				{
					instruction = rMgr.getMemory(rMgr.getRegister(X_REGISTER), 4);
					logList.set(logList.size()-1, "+" + logList.get(logList.size()-1));
					rMgr.setRegister(X_REGISTER, rMgr.getRegister(X_REGISTER) + 4);
				}
				else
				{
					instruction = rMgr.getMemory(rMgr.getRegister(X_REGISTER), 3);
					rMgr.setRegister(X_REGISTER, rMgr.getRegister(X_REGISTER) + 3);
				}
				
				break;
				
			case 0xb4:
				logList.add("CLEAR");
				rMgr.setRegister(X_REGISTER, rMgr.getRegister(X_REGISTER) + 2);
				
				break;
			
			case 0x74:
				logList.add("LDT");
				if(extForm)
				{
					instruction = rMgr.getMemory(rMgr.getRegister(X_REGISTER), 4);
					logList.set(logList.size()-1, "+" + logList.get(logList.size()-1));
					rMgr.setRegister(X_REGISTER, rMgr.getRegister(X_REGISTER) + 4);
				}
				else
				{
					instruction = rMgr.getMemory(rMgr.getRegister(X_REGISTER), 3);
					rMgr.setRegister(X_REGISTER, rMgr.getRegister(X_REGISTER) + 3);
				}
				
				break;
			
			case 0xe0:
				logList.add("TD");
				if(extForm)
				{
					instruction = rMgr.getMemory(rMgr.getRegister(X_REGISTER), 4);
					logList.set(logList.size()-1, "+" + logList.get(logList.size()-1));
					rMgr.setRegister(X_REGISTER, rMgr.getRegister(X_REGISTER) + 4);
				}
				else
				{
					instruction = rMgr.getMemory(rMgr.getRegister(X_REGISTER), 3);
					rMgr.setRegister(X_REGISTER, rMgr.getRegister(X_REGISTER) + 3);
				}
				
				break;
			
			case 0xd8:
				logList.add("RD");
				if(extForm)
				{
					instruction = rMgr.getMemory(rMgr.getRegister(X_REGISTER), 4);
					logList.set(logList.size()-1, "+" + logList.get(logList.size()-1));
					rMgr.setRegister(X_REGISTER, rMgr.getRegister(X_REGISTER) + 4);
				}
				else
				{
					instruction = rMgr.getMemory(rMgr.getRegister(X_REGISTER), 3);
					rMgr.setRegister(X_REGISTER, rMgr.getRegister(X_REGISTER) + 3);
				}
				
				break;
				
			case 0xa0:
				logList.add("COMPR");
				rMgr.setRegister(X_REGISTER, rMgr.getRegister(X_REGISTER) + 2);
				
				break;
			
			case 0x54:
				logList.add("STCH");
				if(extForm)
				{
					instruction = rMgr.getMemory(rMgr.getRegister(X_REGISTER), 4);
					logList.set(logList.size()-1, "+" + logList.get(logList.size()-1));
					rMgr.setRegister(X_REGISTER, rMgr.getRegister(X_REGISTER) + 4);
				}
				else
				{
					instruction = rMgr.getMemory(rMgr.getRegister(X_REGISTER), 3);
					rMgr.setRegister(X_REGISTER, rMgr.getRegister(X_REGISTER) + 3);
				}
				
				break;
			
			case 0xb8:
				logList.add("TIXR");
				rMgr.setRegister(X_REGISTER, rMgr.getRegister(X_REGISTER) + 2);
				
				break;
			
			case 0x38:
				logList.add("JLT");
				if(extForm)
				{
					instruction = rMgr.getMemory(rMgr.getRegister(X_REGISTER), 4);
					logList.set(logList.size()-1, "+" + logList.get(logList.size()-1));
					rMgr.setRegister(X_REGISTER, rMgr.getRegister(X_REGISTER) + 4);
				}
				else
				{
					instruction = rMgr.getMemory(rMgr.getRegister(X_REGISTER), 3);
					rMgr.setRegister(X_REGISTER, rMgr.getRegister(X_REGISTER) + 3);
				}
				
				break;
			
			case 0x10:
				logList.add("STX");
				if(extForm)
				{
					instruction = rMgr.getMemory(rMgr.getRegister(X_REGISTER), 4);
					logList.set(logList.size()-1, "+" + logList.get(logList.size()-1));
					rMgr.setRegister(X_REGISTER, rMgr.getRegister(X_REGISTER) + 4);
				}
				else
				{
					instruction = rMgr.getMemory(rMgr.getRegister(X_REGISTER), 3);
					rMgr.setRegister(X_REGISTER, rMgr.getRegister(X_REGISTER) + 3);
				}
				
				break;
				
			case 0x30:
				logList.add("JEQ");
				if(extForm)
				{
					instruction = rMgr.getMemory(rMgr.getRegister(X_REGISTER), 4);
					logList.set(logList.size()-1, "+" + logList.get(logList.size()-1));
					rMgr.setRegister(X_REGISTER, rMgr.getRegister(X_REGISTER) + 4);
				}
				else
				{
					instruction = rMgr.getMemory(rMgr.getRegister(X_REGISTER), 3);
					rMgr.setRegister(X_REGISTER, rMgr.getRegister(X_REGISTER) + 3);
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
		System.out.println("PC: " + rMgr.getRegister(X_REGISTER));
		for(int i = 0; i < logList.size(); i++)
		{
			System.out.print(logList.get(i) + " ");
		}
		System.out.println();
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
