package encoding;
import java.io.*;
import java.net.*;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;

import javax.sound.sampled.*;


/*
 * creates a socket through udp that recieves the audio byte packet and plays it back while buffering.
 */
public class encoding_server {

ByteArrayOutputStream byteOutputStream;
AudioFormat adFormat;
TargetDataLine targetDataLine;
AudioInputStream InputStream;
SourceDataLine sourceLine;

/*
 * specification declaration of audio formal
 */

private AudioFormat getAudioFormat() 
{	
	AudioFormat.Encoding encoding = AudioFormat.Encoding.PCM_SIGNED;
	float sampleRate = 16000.0F;
    int sampleInbits = 16;
    int channels = 1;
    boolean signed = true;
    boolean bigEndian = false;
    return new AudioFormat(sampleRate, sampleInbits, channels, signed, bigEndian);
}

public static void main(String args[]) {
    /*
     * calls the run application to play the recieved audio
     */
	new encoding_server().runVOIP();
	//delay();
}

public void runVOIP() {
    try {
    	/*
    	 * create a datagramsocket that binds to local host and the specified port address
    	 */
        DatagramSocket serverSocket = new DatagramSocket(7780);
       // System.out.println("hello");
        byte[] receiveData = new byte[10240];
       // System.out.println("hello1");
        File file =new File("time_server.txt");
        if(file.exists())
		{
			file.delete();
		}
        file.createNewFile();
        FileWriter fileWritter;
        BufferedWriter bufferWritter = null;
        while (true) {
        	/*
        	 * while recieving the datagram packet, write the byte packet that transmits the databye from buffer to speakers.
        	 */
            
            
            
            
            ///////////////////////////////////
            byte audioData[]=new byte[10240];
            
            for(int loopvar=0;loopvar<40;loopvar++){
            	
            	DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                System.out.println("receivePacket");
                serverSocket.receive(receivePacket);
                if(!file.exists()){
                	file.createNewFile();
            		//file.delete();
        		}
                fileWritter = new FileWriter(file.getName(),true);
    	        bufferWritter = new BufferedWriter(fileWritter);
    	        /*long time = System.currentTimeMillis();
                Timestamp timestamp = new Timestamp(time);
                int nanos = timestamp.getNanos(); 
                System.out.println(nanos); 
    	        bufferWritter.write(nanos+"\n");*/
                
    	 	   	
                System.out.println("RECEIVED: " + receivePacket.getAddress().getHostAddress() + " " + receivePacket.getPort());
                byte audioData2[] = receivePacket.getData();
            	
            	
        		byte[] tempBuffer1= new byte[256];
        		for(int loopvar2=0;loopvar2<256;loopvar2++){
        			audioData[256*loopvar+loopvar2]=audioData2[loopvar2];
        		}
        		
            }
            DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");	        	  
	 	   	//get current date time with Calendar()
	 	   	Calendar cal = Calendar.getInstance();
	 	   bufferWritter.write(dateFormat.format(cal.getTimeInMillis())+"\n");
           bufferWritter.close();
            
            /*char gen=90;
            int[] tempBuffer3=new int[256];
        	for(int lv=0;lv<256;lv++){
        		tempBuffer3[lv]=(int)(audioData2[lv]);
        		if(tempBuffer3[lv]<0){
        			tempBuffer3[lv]+=256;
        		}
        	}
        	System.out.println("zxc");
        	Decoder d = new Decoder(gen, 11);
        	
    		int[] bads = {};
    		HashSet<Integer> bad = new HashSet<Integer>(1);
    		int[] c4 = d.decode257(tempBuffer3, bad);
    		
    		byte[] audioData=new byte[c4.length];
        	for(int lv=0;lv<c4.length;lv++){
        		audioData[lv]=(byte)(c4[lv]);
        	}
            */
            
            
            
            
            
            
            try {
            	/*
            	 * get the data using getdata(), sourceline subinterfaces of dataline
            	 * initialise the data array to the type byte
            	 * open the audio and start buffering that can also be used to create a .wav file to save the audio recording to a specified folder
            	 */
            	
            
                
                
                ///////////////////////////////////
                InputStream byteInputStream = new ByteArrayInputStream(audioData);
                AudioFormat adFormat = getAudioFormat();
                InputStream = new AudioInputStream(byteInputStream, adFormat, audioData.length / adFormat.getFrameSize());
                DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, adFormat);
                sourceLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
                //System.out.println("hello");
                sourceLine.open(adFormat);
                sourceLine.start();
                
                
                /*
                 * play the stored thread in buffer till packets are recieved.
                 */
                Thread playThread = new Thread(new PlayThread());
                playThread.start();
                System.out.println(dateFormat.format(cal.getTimeInMillis())); 
    	        
            } catch (Exception e) {
                System.out.println(e);
                System.exit(0);
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
}

class PlayThread extends Thread {

    byte tempBuffer[] = new byte[10240];

    public void run() {
        try {
            int cnt;
            /*
             * to play back the audio recording, write command of socket sends the data bytes from buffer to speakers.
             */
            while ((cnt = InputStream.read(tempBuffer, 0, tempBuffer.length)) != -1) {
                if (cnt > 0) {
                    sourceLine.write(tempBuffer, 0, cnt);
                }
            }
            //delay();
        } catch (Exception e) {
            System.out.println(e);
            System.exit(0);
        }
    }
}
}