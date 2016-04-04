// by Chen Qinwu
package zhang.stu.NewInputMethod;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import android.util.Log;

public class KingDimEngine {

	public static void readFile(InputStream inputStream) {
//	KingDimInputMethodService mService = new KingDimInputMethodService();
		try {
			NumGB=inputStream.available()/4;
			gb1zbz = new int[NumGB];
//			InputStream inputStream = mService.getResources().openRawResource(R.raw.lib1zbz);
			for (int i = 0; i < NumGB; i++) {
				gb1zbz[i] = inputStream.read() + (inputStream.read() << 8)
						+ (inputStream.read() << 16)+ (inputStream.read() << 24);
				// System.out.println(gb1zbz[i]);
			}
			inputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	KingDimEngine(InputStream is) {// throws IOException {
		System.out.println("qw Constructing Engine");
		readFile( is );
		System.out.println("qw closeFile");
	}

	public static String toCChar(int hzOrder) throws UnsupportedEncodingException {
		// ���ִ���ֵת���ɺ�������
		int qu=0, wei=0; // ��λ��
		if(hzOrder<6768){
		// '��'����0xB0A1=0x1001+0xA0A0
			qu = hzOrder / 94 + 0xB0;
			wei = hzOrder % 94 + 0xA1;
		}else {
			hzOrder-=6768;
			if (hzOrder < 6080) {
				qu = hzOrder / 190 + 0x81;
				wei = hzOrder % 190 + 0x40;
			} else {
				hzOrder -= 6080;
				if (hzOrder < 8160) {
					qu = hzOrder / 96 + 0xAA;
					wei = hzOrder % 96 + 0x40;
				}
			}
			if (wei >= 0x7F)
				wei += 1;
		}
		byte[] b = new byte[2];
		b[0] = (byte) qu;	b[1] = (byte) wei;
		String s = new String(b, "GBK");
		return s;	//(char) (qu * 256 + wei);
	}
	public List<String> getCandidates(StringBuilder mComposing) {
		String mComposingStr = mComposing.toString();
		str2Hex(mComposingStr);
		List<String> suggestions = new ArrayList<String>();
// locate();
		int count = 0, CurrentOrder = 0;
		while (CurrentOrder < NumGB && count < 30) {
			if ((gb1zbz[CurrentOrder] & iMo) == iCode) {
			//	System.out.print(CurrentOrder);
				try {
					suggestions.add(toCChar(CurrentOrder));
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				count++;
			}
			CurrentOrder++;
		}     
		return suggestions;
	}

	static int iCode, iMo;

	/*
	 	public static void str2Hex(String InCode) { // ͬʱ����ģiMo
		//System.out.println("qw:" + InCode);
		int index[]={20,24,0,4,8,28,12};//������ƴ����ĸ�룬��ǰ��mo1��ȡ��
		if(KingDimInputMethodService.Mode == 1)//�ʻ����뷨ʱ��˳��
		{
			index[0]= 0;
			index[1]= 4;
			index[2]= 8;
			index[3]= 12;
			index[4]= 16;
			index[5]= 20;
			index[6]= 24;
		}
		else if(KingDimInputMethodService.Mode == 0)
		{
			index[0]= 20;
			index[1]= 24;
			index[2]= 0;
			index[3]= 4;
			index[4]= 8;
			index[5]= 28;
			index[6]= 12;
		}
		else
		{;}
		iCode = iMo = 0;
		int idx = InCode.length()-1;
		if(idx>6)idx=6;
		while (idx >= 0) {
			char ch1 = InCode.charAt(idx);
			// System.out.print(ch1);
			if (ch1 != '*') // ��*��ģ����
			{
				if((ch1=='0')&&(idx<=1))
					ch1 = 10;
				if((ch1=='0')&&(idx>1))
					ch1 = 10;
				int sh = index[idx];
				iCode |= (ch1 & 0xF) << sh;
				iMo |= 0xF << sh;
//				//System.out.println("iCode:" + iCode);
//				//System.out.println("iMo:" + iMo);
			}
			idx--;
		}
		return;
	}
	*/
//	/*
	public static void str2Hex(String InCode) { // ͬʱ����ģiMo
		//System.out.println("qw:" + InCode);
		int indexArray[][]={{20,24,0,4,8,28,12},{0,4,8,12,16,20,24}};
		int []index = new int[7];//������ƴ����ĸ�룬��ǰ��mo1��ȡ��
		iCode = iMo = 0;
		int idx = InCode.length()-1;
		System.out.println(idx+"   idx");
		if(idx>6)idx=6;
		if(KingDimInputMethodService.Mode == 1)//�ʻ����뷨ʱ��˳��
		{
		  index=indexArray[1];
		  while (idx >= 0) {
				char ch1 = InCode.charAt(idx);
				// System.out.print(ch1);
				if (ch1 != '*') // ��*��ģ����
				{
					System.out.println(KingDimInputMethodService.Mode+"   mode");
						
							if((ch1=='0')&&(idx>2))
							{
								System.out.println(idx+"   here");
								ch1 = 10;
							}
					int sh = index[idx];
					iCode |= (ch1 & 0xF) << sh;
					iMo |= 0xF << sh;
					System.out.println("idx:" + idx);
					System.out.println("iCode:" + iCode);
					System.out.println("iMo:" + iMo);
				}
				idx--;
			}
		}
		else if(KingDimInputMethodService.Mode == 0)
		{
		 index=indexArray[0];
		 while (idx >= 0) {
				char ch1 = InCode.charAt(idx);
				// System.out.print(ch1);
				if (ch1 != '*') // ��*��ģ����
				{
					System.out.println(KingDimInputMethodService.Mode+"   mode");
					if((ch1=='0')&&(idx<=1))
					{
						System.out.println(idx+"   here");
							ch1 = 10;
					}
					int sh = index[idx];
					iCode |= (ch1 & 0xF) << sh;
					iMo |= 0xF << sh;
					System.out.println("idx:" + idx);
					System.out.println("iCode:" + iCode);
					System.out.println("iMo:" + iMo);
				}
				idx--;
			}
		}
		else
		{;}
		return;
	}
//	*/
	
	
	
	static int NumGB;
	static int gb1zbz[];
//	static final int NumGB = 21008;
//	static int[] gb1zbz = new int[NumGB];
}