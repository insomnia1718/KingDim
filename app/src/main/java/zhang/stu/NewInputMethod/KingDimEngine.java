package zhang.stu.NewInputMethod;
//尝试将用户最近输入的汉字信息存在/data/data下，但是向那个文件夹下写文件是Activity的方法，而不是Service的方法，所以每次都写不进去
//因此读到的每次都是最常用的512个汉字
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;

public class KingDimEngine {
    
	private Context context;
	//
	public  void readFile(InputStream inputStream,InputStream InitCache,InputStream InitCacheOrder) {
//	KingDimInputMethodService mService = new KingDimInputMethodService();
		//先从/data/data下的cache.dat中读最近的用户数据。如果是第一次使用，则读取cacheinit.dat中的日常使用频率最高的512个字
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
			CacheSize = InitCache.available()/4;
			Cache = new int[CacheSize];
			CacheOrder = new int[CacheSize];
	        try{ 
	        	FileInputStream fin = context.openFileInput("Cache"); 
	        	System.out.println("Read Cache!");
	        	int length = fin.available(); 
	         	byte [] buffer = new byte[length]; 
	         	fin.read(buffer);     
	         	// res = EncodingUtils.getString(buffer, "UTF-8"); 
	         	fin.close();     
	         	for(int i=0;i<CacheSize;i++)
	         	{
	         		CacheOrder[i] = (buffer[4*i]&0xff) + ((buffer[4*i+1]&0xff)<<8) +((buffer[4*i+2]&0xff)<<16) +((buffer[4*i+3]&0xff)<<24);
	         	}
	        }catch(NullPointerException errr)
	        {
	        	System.out.println("Read Init File!");
	        	try{
	        		for (int i = 0; i <CacheSize; i++) {
	        				Cache[i] = InitCache.read() + (InitCache.read() << 8)
	        				+ (InitCache.read() << 16)+ (InitCache.read() << 24);
	        				CacheOrder[i]=InitCacheOrder.read() + (InitCacheOrder.read() << 8)
	        				+ (InitCacheOrder.read() << 16)+ (InitCacheOrder.read() << 24);
	        				// System.out.println(gb1zbz[i]);
	        		}
	        		InitCache.close();
	        	}catch(Exception err){ 
	   	         err.printStackTrace(); 
	         	System.out.println("11111!");
	        	}
	        }
	        catch(Exception e){ 
	         e.printStackTrace(); 
	        	System.out.println("22222!");
	        } 		
		} catch (IOException e) {
			e.printStackTrace();
        	System.out.println("33333!");
		}
	}

	KingDimEngine(InputStream is, InputStream InitCache,InputStream InitCacheOrder) {// throws IOException {
		System.out.println(" Constructing Engine");
		readFile( is,InitCache,InitCacheOrder);
		this.context=context;
	}

	public static String toCChar(int hzOrder) throws UnsupportedEncodingException {
		// 汉字次序值转换成汉字内码
		int qu=0, wei=0; // 区位码
		if(hzOrder<6768){
		// '啊'内码0xB0A1=0x1001+0xA0A0
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
		
		while(CurrentOrder<CacheSize && count<30){
			 if((Cache[CurrentOrder] & iMo)== iCode)
			 {
				 try {
						suggestions.add(toCChar(CacheOrder[CurrentOrder]));//每次用户选择汉字后开始调整cache中汉字的位置
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}		
					count++;
				}
				CurrentOrder++;
			 }	
		CurrentOrder = 0 ;
		while (CurrentOrder < NumGB && count < 30) {
			if ((gb1zbz[CurrentOrder] & iMo) == iCode) {
			//	System.out.print(CurrentOrder);
				try {
					if(!isContained(toCChar(CurrentOrder),suggestions))
					{
						suggestions.add(toCChar(CurrentOrder));
					}
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

	public static void str2Hex(String InCode) { // 同时产生模iMo
		//System.out.println("qw:" + InCode);
		int indexArray[][]={{20,24,0,4,8,28,12},{0,4,8,12,16,20,24}};
		int []index = new int[7];//按先首拼，韵母码，再前三mo1笔取码
		iCode = iMo = 0;
		int idx = InCode.length()-1;
		if(idx>6)idx=6;
		if(KingDimInputMethodService.Mode == 1)//笔画输入法时的顺序
		{
		  index=indexArray[1];
		  while (idx >= 0) {
				char ch1 = InCode.charAt(idx);
				// System.out.print(ch1);
				if (ch1 != '*') // 用*作模糊码
				{
						if((ch1=='0')&&(idx<=1))
							ch1 = 10;
					int sh = index[idx];
					iCode |= (ch1 & 0xF) << sh;
					iMo |= 0xF << sh;
//					//System.out.println("iCode:" + iCode);
//					//System.out.println("iMo:" + iMo);
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
				if (ch1 != '*') // 用*作模糊码
				{
						if((ch1=='0')&&(idx<1))
							ch1 = 10;
					int sh = index[idx];
					iCode |= (ch1 & 0xF) << sh;
					iMo |= 0xF << sh;
//					//System.out.println("iCode:" + iCode);
//					//System.out.println("iMo:" + iMo);
				}
				idx--;
			}
		}
		else
		{;}
		return;
	}
		
	static int NumGB;
	static int gb1zbz[];
	static int CacheSize=512;
	static int Cache[];
	static int CacheOrder[];
	static final int GB2312CNT = 6768;	//GB2312的字数量
	static final int GBK3CNT = 6080;	//GBK3的字数量
	static final int GBK4CNT = 8160;	//GBK4的字数量
	static final int GBK3GBCNT = 12848;	//= GBK3COUNT+GB2312COUNT = 6080+6768
//	static final int NumGB = 21008;
//	static int[] gb1zbz = new int[NumGB];
	
		
public void writeFileData(String fileName,byte[] bytes){ 
	       try{ 
	         FileOutputStream fout =context.openFileOutput(fileName, Context.MODE_PRIVATE);
	         fout.write(bytes); 
	         fout.close(); 
	         System.out.println("write cache file");
	        } 
	       catch(Exception e){ 
	        e.printStackTrace(); 
	       } 
	   }

public void updateCache(String word)//更新cache中汉字的顺序。分成原来cache里有这个汉字和没这个汉字两种情况
{
	Charset cst = Charset.forName("gbk");
	ByteBuffer buffer = cst.encode(word);
	byte[] bytes = buffer.array();//得到输入单字的GBK编码
	int OrderCode=0,i=0,temp=0,tmp=0;
	boolean Flage = false;
	OrderCode = toOrder(bytes[0],bytes[1]);//得到输入汉字的次序码 
	System.out.println("OrderCode = "+OrderCode);
	for(i=0;i<CacheSize;i++)
	{
		if((CacheOrder[i]==OrderCode))
		{
			if(i==0)
			{
				Flage = true;
				System.out.println("i==0");
			}
			else{
				System.out.println("i!=0");
				temp = Cache[i];
				tmp = CacheOrder[i];
				System.out.println("the cache order is"+tmp);
				for(int j=i;j>0;j--)
				{
					Cache[j]=Cache[j-1];
					CacheOrder[j]=CacheOrder[j-1];
				}
				Cache[0]=temp;
				CacheOrder[0]=tmp;
				Flage = true;
			}
		}
	}
	if(Flage==false)
	{
		for(i=CacheSize-1;i>0;i--)
		{
			Cache[i] = Cache[i-1];
			CacheOrder[i]=CacheOrder[i-1];
		}
		    Cache[0] = gb1zbz[OrderCode];
			CacheOrder[0] = OrderCode;
	}
	//saveCache();
	writeFile("Cache","testing");
}
public boolean isContained(String word, List<String> suggestions)//判断之前是否已经检索到这个汉字
{
	int i=0;
	for(i=0;i<suggestions.size();i++)
	{
		if(suggestions.get(i).equals(word))
		{
			return true;
		}
	}
	return false;
}

/*********************************************************************/
int toOrder(byte q,byte w)	//qw__inline WORD!
{
	
	int qu=0,wei=0;
	qu = q & 0xff;
	wei = w & 0xff;
	if((qu>=0xB0) && (qu <=0xF7)&&(wei>=0xA1))//&&(wei<=0xFE)) //GB2312
	{
		return (qu-0xB0)*94+wei-0xA1;
	}
	if(wei>=0x7F)
		wei=wei-1;
	if(qu<=0xA0)//GBK3((qu>=0x81) && (qu <=0xA0)&&(wei>=0x40)&&(wei<=0xFE))
		return (qu-0x81)*190+wei-0x40+GB2312CNT;
	if((qu>=0xAA)&&(wei<=0xA0-1)) //GBK4 && (qu <=0xFE)&&(wei>=0x40)
	{
		return (qu-0xAA)*96+wei-0x40+GBK3GBCNT;
	}	
	return 3758;//no any word code;
}
/*********************************************************************/

public void saveCache()
{
	  byte[] BackupCache = new byte[4*CacheSize];
	  for(int i=0;i<CacheSize;i++)
	  {
		  BackupCache[4*i]= (byte) (CacheOrder[i]&0xff);
		  BackupCache[4*i+1]= (byte) ((CacheOrder[i]>>8)&0xff);
		  BackupCache[4*i+2]= (byte) ((CacheOrder[i]>>16)&0xff);
		  BackupCache[4*i+3]= (byte) ((CacheOrder[i]>>24)&0xff);	
	  }
	  writeFileData("Cache", BackupCache);
}

public void writeFile(String fileName,String message){ 
    try{ 
     FileOutputStream fout =context.openFileOutput(fileName,Context.MODE_PRIVATE);
     byte [] bytes = message.getBytes(); 
     fout.write(bytes); 
      fout.close(); 
     } 
    catch(Exception e){ 
     e.printStackTrace(); 
    } 
}
}