package encoding;
import com.onionnetworks.fec.FECCode;
import com.onionnetworks.fec.FECCodeFactory;
import com.onionnetworks.util.Buffer;

import java.util.Arrays;

public class JavaFECExample {
    //k = number of source packets to encode
    //n = number of packets to encode to
	static int k=16;
	static int n=32;
	static int packetsize=1024;
	static byte[] repair,packet;
	static Buffer[] sourceBuffer,repairBuffer,receiverBuffer;
	static int[] repairIndex,receiverIndex;
	static FECCode fec;
    
    public Buffer[] enc(byte[] source) {       
        //source = new byte[k*packetsize]; //this is our source file
    	System.out.println("x");
        sourceBuffer = new Buffer[k];
        for (int i = 0; i < sourceBuffer.length; i++)
            sourceBuffer[i] = new Buffer(source, i*packetsize, packetsize);
        
        repair = new byte[n*packetsize];        
        repairBuffer = new Buffer[n];
        
        for (int i = 0; i < repairBuffer.length; i++)
            repairBuffer[i] = new Buffer(repair, i*packetsize, packetsize);  
        repairIndex = new int[n];
        //When sending the data you must identify what it's index was.
        //Will be shown and explained later
        for (int i = 0; i < repairIndex.length; i++)
            repairIndex[i] = i;
        //create our fec code
        fec = FECCodeFactory.getDefault().createFECCode(k,n);
        
        //encode the data
        fec.encode(sourceBuffer, repairBuffer, repairIndex);
        //encoded data is now contained in the repairBuffer/repair byte array
        
        //From here you can send each 'packet' of the encoded data, along with
        //what repairIndex it has.  Also include the group number if you had to
        //split the file
        
        return repairBuffer;
    }
    public byte[] dec(byte[] rec)
    {
    	//We only need to store k, packets received
        //Don't forget we need the index value for each packet too
        receiverBuffer = new Buffer[k];
        receiverIndex = new int[k];
        Buffer[] temp = new Buffer[k];
        byte[] received = new byte[k*packetsize];
        
        System.out.println("lenght:" + rec.length);
        int i = 0;
        //this will store the received packets to be decoded
        for (i = 0; i < temp.length; i++)
            temp[i] = new Buffer(rec, i*packetsize, packetsize);
        
        System.out.println("i" + i);
        
        
        //We will simulate dropping every even packet
        int j = 0; 
        System.out.println("n" + n);
        for (i = 0; i < k; i++) {
            //if (i % 2 == 0)
                //continue;
            packet = temp[i].getBytes();
            System.arraycopy(packet, 0, received, j*packetsize, packet.length);
            receiverIndex[j] = i;
            j++;
            System.out.println("packetlength"+packet.length);
            System.out.println("j:"+j);
            
        }
        System.out.println("jfinal:"+j);
        System.out.println("recieved:"+received.length);
        //create our Buffers for the encoded data
        for (i = 0; i < k; i++)
            receiverBuffer[i] = new Buffer(received, i*packetsize, packetsize);
        
        //finally we can decode
        fec.decode(receiverBuffer, receiverIndex);
        
        //check for equality
        return received;
        
    }
    public boolean check(byte[] source,byte[] received)
    {
    	if (Arrays.equals(source, received))
        {
    		System.out.println("Source and Received Files are equal!");
    		return true;
        }
        else
        {
            System.out.println("Source and Received Files are different!");
            return false;
        }
    }
}