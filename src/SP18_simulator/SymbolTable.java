package SP18_simulator;

import java.util.ArrayList;

/**
 * symbol�� ���õ� �����Ϳ� ������ �����Ѵ�. section ���� �ϳ��� �ν��Ͻ��� �Ҵ��Ѵ�.
 */
public class SymbolTable
{
	ArrayList<String> symbolList;
	ArrayList<Integer> addressList;
	// ��Ÿ literal, external ���� �� ó������� �����Ѵ�.
	
	// ���� ����� ���� ����Ʈ
	ArrayList<Integer> modifSizeList;
	// ���� ���, �� ��ȣ�� ���� ����Ʈ
	ArrayList<Character> modifModeList;
	// ������ ���� ���α׷� ��ȣ�� ���� ����Ʈ
	ArrayList<Integer> sectionList;
	

	public SymbolTable()
	{
		symbolList = new ArrayList<>();
		addressList = new ArrayList<>();
		modifSizeList = new ArrayList<>();
		modifModeList = new ArrayList<>();
		sectionList = new ArrayList<>();
	}

	/**
	 * ���ο� Symbol�� table�� �߰��Ѵ�.
	 * 
	 * @param symbol
	 *            : ���� �߰��Ǵ� symbol�� label
	 * @param address
	 *            : �ش� symbol�� ������ �ּҰ� <br>
	 * 			<br>
	 *            ���� : ���� �ߺ��� symbol�� putSymbol�� ���ؼ� �Էµȴٸ� �̴� ���α׷� �ڵ忡 ������ ������ ��Ÿ����.
	 *            ��Ī�Ǵ� �ּҰ��� ������ modifySymbol()�� ���ؼ� �̷������ �Ѵ�.
	 */
	public void putSymbol(String symbol, int address)
	{
		String inputSymbol = symbol;
		if (!symbolList.contains(inputSymbol))
		{
			// �ɺ��� ���ڷ� ���� �ּҰ��� ������
			symbolList.add(inputSymbol);
			addressList.add(address);
		}

	}
	
	/**
	 * ������ ���� extab�� ������ �߰��ϴ� �޼ҵ��̴�.
	 * @param symbol �߰��� �ɺ�
	 * @param address ������ �ּ�
	 * @param modifSize ������ ������
	 * @param modifMode ���� ���, �� ��ȣ
	 * @param section ������ ��Ʈ�� ����
	 */
	public void putExSymbol(String symbol, int address, int modifSize, char modifMode, int section)
	{
		symbolList.add(symbol);
		addressList.add(address);
		modifSizeList.add(modifSize);
		modifModeList.add(modifMode);
		sectionList.add(section);
	}

	/**
	 * ������ �����ϴ� symbol ���� ���ؼ� ����Ű�� �ּҰ��� �����Ѵ�.
	 * 
	 * @param symbol
	 *            : ������ ���ϴ� symbol�� label
	 * @param newaddress
	 *            : ���� �ٲٰ��� �ϴ� �ּҰ�
	 */
	public void modifySymbol(String symbol, int newaddress)
	{
		String inputSymbol = symbol;

		// List �� �̹� ����Ǿ��ִ� ��쿡�� ������ ����
		if (symbolList.contains(inputSymbol))
		{
			// ����Ǿ��ִ� �ɺ��� ��ġ�� ã�� ���ڷ� ���� ���ο� �ּҰ��� �־���
			for (int index = 0; index < symbolList.size(); index++)
				if (inputSymbol.equals(symbolList.get(index)))
				{
					symbolList.set(index, inputSymbol);
					addressList.set(index, newaddress);
					break;
				}
		}
	}

	/**
	 * ���ڷ� ���޵� symbol�� � �ּҸ� ��Ī�ϴ��� �˷��ش�.
	 * 
	 * @param symbol
	 *            : �˻��� ���ϴ� symbol�� label
	 * @return symbol�� ������ �ִ� �ּҰ�. �ش� symbol�� ���� ��� -1 ����
	 */
	public int search(String symbol)
	{
		// ����� �ּҰ� ����
		int address = 0;

		// ���ڷ� ���� �ɺ��� List �� �ִ� ���
		// �ش� �ɺ��� �ּҰ��� ã�� address�� ����
		if (symbolList.contains(symbol))
		{
			for (int index = 0; index < symbolList.size(); index++)
				if (symbol.equals(symbolList.get(index)))
				{
					address = addressList.get(index);
					break;
				}
		}
		// ���� ��� -1�� address�� ����
		else
			address = -1;

		// address ����
		return address;
	}
	
	/**
	 * �ɺ� ���̺��� ����� ���Ѵ�
	 * @return ���̺� ������
	 */
	public int size()
	{
		return symbolList.size();
	}
	
	/**
	 * �ش� �ε����� �ɺ��� �����´�.
	 * @param index ������ �ɺ� �ε���
	 * @return �ɺ� �̸�
	 */
	public String getSymbol(int index)
	{
		return symbolList.get(index);
	}
	
	/**
	 * �ش� �ε����� �ּҸ� �����´�.
	 * @param sectionNum ������ �ּ� �ε���
	 * @return �ּ�
	 */
	public int getaddress(int index)
	{
		return addressList.get(index);
	}
	
	/**
	 * �ش� �ε����� ���� ����� �����´�.
	 * @param sectionNum ������ ���� ������ �ε���
	 * @return ���� ������
	 */
	public int getModifSize(int index)
	{
		return modifSizeList.get(index);
	}
	
	/**
	 * �ش� �ε����� ���� ��� �����´�.
	 * @param sectionNum ������ ���� ��� �ε���
	 * @return ���� ���
	 */
	public char getModifMode(int index)
	{
		return modifModeList.get(index);
	}
	
	/**
	 * �ش� �ε����� ������ �����´�.
	 * @param sectionNum ������ ���� �ε���
	 * @return ����
	 */
	public int getSection(int index)
	{
		return sectionList.get(index);
	}
	
}
