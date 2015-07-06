package encoding;

import java.awt.*;
import java.io.*;

import javax.sound.sampled.*;
import javax.swing.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import com.onionnetworks.util.Buffer;

import java.text.*
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.io.*
import java.sql.Timestamp;
/*
 * This is an example of Socket Programming through UDP that captures an audio recording through michrophone for user defined time in bytes and transfer the same to the server side.
 * This uses a GUI option pane to capture, stop and playback the recording.
 */
public class encoding extends JFrame{
	/*
	 * send_audio class is Client Class that sends the audio file to server.
	 */
	private static final long serialVersionUID = 1L;
	boolean stopaudioCapture = false;
	ByteArrayOutputStream byteOutputStream;
	AudioFormat adFormat;
	TargetDataLine targetDataLine;
	AudioInputStream InputStream;
	SourceDataLine sourceLine;
	static JavaFECExample j;
	Graphics g;
	AudioFormat.Encoding encoding1 = AudioFormat.Encoding.PCM_SIGNED;

	/*
	 * main() calls constructor of the class 
	 */
	public static void main(String args[]) {
		new encoding();
	}
	public encoding() {
	    final JButton capture = new JButton("Capture");
	    final JButton stop = new JButton("Stop");
	    final JButton play = new JButton("Playback");
	    final JButton delay1 = new JButton("Calculate Delay");
	    

	    capture.setEnabled(true);
	    stop.setEnabled(false);
	    play.setEnabled(false);
	    delay1.setEnabled(false);
	    /*
	     * Setting of buttons to control the input "capture, stop, playback"
	     * Initially, capture button's action is set to true, stop is disabled and playback button is also enabled while capturing 
	     */
	    capture.addActionListener(new ActionListener() {
	        public void actionPerformed(ActionEvent e) {
	            capture.setEnabled(false);
	            stop.setEnabled(true);
	            play.setEnabled(false);
	            /*
	             * calls capture function to record the audio from microphone
	             */
	            captureAudio();
	        }
	    });
	    /*
	     * adds the button to the GUI window 
	     */
	    getContentPane().add(capture);

	    /*
	     * It stops the recording while capturing the audio.
	     */
	    stop.addActionListener(new ActionListener() {
	        public void actionPerformed(ActionEvent e) {
	            capture.setEnabled(true);
	            stop.setEnabled(false);
	            play.setEnabled(true);
	            delay1.setEnabled(true);
	            stopaudioCapture = true;
	            targetDataLine.close();
	        }
	    });
	    /*
	     * adds the button to the GUI window
	     */
	    getContentPane().add(stop);

	    /*
	     * to play the audio, new actionlistiner is called 
	     */
	    play.addActionListener(new ActionListener() {
	        @Override
	        public void actionPerformed(ActionEvent e) {
	            playAudio();
	        }
	    });
	    /*
	     * adds button to the GUI window
	     */
	    getContentPane().add(play);
	    delay1.addActionListener(new ActionListener() {
	        @Override
	        public void actionPerformed(ActionEvent e) {
	            //delay1.setEnabled(true);
	        	cal_delay();
	        }
	    });
	    /*
	     * adds button to the GUI window
	     */
	    getContentPane().add(delay1);

	    /*
	     * defines the Name, Size, Color and position of the GUI window.
	     */
	    getContentPane().setLayout(new FlowLayout());
	    setTitle("Capture/Playback Demo");
	    setDefaultCloseOperation(EXIT_ON_CLOSE);
	    setSize(400, 100);
	    getContentPane().setBackground(Color.white);
	    setVisible(true);
	    g = (Graphics) this.getGraphics();
	}
	
	
	/*
	 * Dataline adds media related functionality to superinterface LINE.
	 * outgoing audio data that is being captured is stored in the internal buffer of dataline
	 * 
	 */
	
	private void captureAudio() {
	    try {
	    	/*
	    	 * targetdataline allows data to be read in byte streams
	    	 */
	        adFormat = getAudioFormat();
	        DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, adFormat);
	        targetDataLine = (TargetDataLine) AudioSystem.getLine(dataLineInfo);
	        targetDataLine.open(adFormat);
	        targetDataLine.start();

	        Thread captureThread = new Thread(new CaptureThread());
	        /*
	         * Start event of the thread begins or ceases active presentation of the data
	         */
	        captureThread.start();
	    } catch (Exception e) {
	    	/*
	    	 * event handler is used in case of any byte requirements
	    	 */
	        StackTraceElement stackEle[] = e.getStackTrace();
	        for (StackTraceElement val : stackEle) {
	            System.out.println(val);
	        }
	        System.exit(0);
	    }
	}
	
	private void cal_delay()
	{
		try{
			Thread cd = new Thread(new Delay());
	        cd.start();
		}
		catch(Exception e) {
	        System.out.println(e);
	        System.exit(0);
	    }
	}

	private void playAudio() {
	    try {
	    	/*
	    	 * plays the byte stream stored in internal buffer
	    	 */
	        byte audioData[] = byteOutputStream.toByteArray();
	        InputStream byteInputStream = new ByteArrayInputStream(audioData);
	        AudioFormat adFormat = getAudioFormat();
	        InputStream = new AudioInputStream(byteInputStream, adFormat, audioData.length / adFormat.getFrameSize());
	        DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, adFormat);
	        sourceLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
	        sourceLine.open(adFormat);
	        sourceLine.start();
	        Thread playThread = new Thread(new PlayThread());
	        playThread.start();
	    } catch (Exception e) {
	        System.out.println(e);
	        System.exit(0);
	    }
	}
	
	
	private AudioFormat getAudioFormat() 
	{	
		
		float sampleRate = 16000.0F;
	    int sampleInbits = 16;
	    int channels = 1;
	    boolean signed = true;
	    boolean bigEndian = false;
	    return new AudioFormat(sampleRate, sampleInbits, channels, signed, bigEndian);
	}

	class CaptureThread extends Thread 
	{
	    /*
	     * create a socket using datagramspcket api to connect to the server and binds it on localhost with specified port address
	     * create a buffer stream and send the recording to the server 
	     */
		byte[] tempBuffer= new byte[10240];

		File file =new File("time_client.txt");
	    boolean bool = false;
	    public void run() 
	    {
	    	
	        byteOutputStream = new ByteArrayOutputStream();
	        stopaudioCapture = false;
	        FileWriter fileWritter = null;
	        BufferedWriter bufferWritter = null;
	        Buffer[] free;
	        
	        try 
	        {
	            DatagramSocket clientSocket = new DatagramSocket(9780);
	            InetAddress IPAddress = InetAddress.getByName("127.0.0.1");
	            
	            if(file.exists()){
	            	file.delete();
	    		}
	            file.createNewFile();
	    		fileWritter = new FileWriter(file.getName(),true);
		        bufferWritter = new BufferedWriter(fileWritter);
	            while (!stopaudioCapture) 
	            {
	                int cnt = targetDataLine.read(tempBuffer, 0, tempBuffer.length);
	                
	                if (cnt > 0) 
	                {
	                	// everything is stored in temp buffer
	                	System.out.println("x");
	                	//j=new JavaFECExample();
	                	//free=j.enc(tempBuffer);
	                    //DatagramPacket sendPacket = new DatagramPacket(free.toString().getBytes(), free.length, IPAddress, 7780);
	                	//clientSocket.send(sendPacket);
	                	//byteOutputStream.write(free.toString().getBytes(), 0, cnt);
	                	Byte n=0;
	                	/*for(int gh=0;gh<tempBuffer.toString().getBytes().length;gh++){
	                		n= tempBuffer[gh];
	                		System.out.print(n.intValue()+" ");
	                	}
	                	System.out.println();*/
	                	GF28.init();
	            		GF257.init();
	     
	                	int s = 5;
	                	char gen=90;
	                	
	                	DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");	        	  
	        	 	   	//get current date time with Calendar()
	        	 	   	Calendar cal = Calendar.getInstance();
	        	 	   	System.out.println(dateFormat.format(cal.getTimeInMillis())); 
	        	        bufferWritter.write(dateFormat.format(cal.getTimeInMillis())+ "\n");
	                	for(int loopvar=0;loopvar<40;loopvar++){
	                		byte[] tempBuffer1= new byte[256];
	                		for(int loopvar2=0;loopvar2<256;loopvar2++){
	                			tempBuffer1[loopvar2]=tempBuffer[256*loopvar+loopvar2];
	                		}
	                	
	                	
	                	Encoder e = new Encoder(tempBuffer1.toString(), s, gen);
	                	int[] cFFT = e.slow257();
	                	//System.out.println("here2");
	                	byte[] tempBuffer2=new byte[cFFT.length];
	                	for(int lv=0;lv<cFFT.length;lv++){
	                		tempBuffer2[lv]=(byte)(cFFT[lv]);
	                	}
	                	//System.out.println(tempBuffer.toString().length());
	                	//System.out.println("here");
	                	//decoder
	                	int[] tempBuffer3=new int[tempBuffer2.length];
	                	for(int lv=0;lv<tempBuffer2.length;lv++){
	                		tempBuffer3[lv]=(int)(tempBuffer2[lv]);
	                		if(tempBuffer3[lv]<0){
	                			tempBuffer3[lv]+=256;
	                		}
	                	}
	                	//System.out.println("here");
	                	Decoder d = new Decoder(gen, tempBuffer1.toString().length());
	                	//System.out.println("here");
	            		int[] bads = {};
	            		HashSet<Integer> bad = new HashSet<Integer>(1);
	            		int[] c4 = d.decode257(tempBuffer3, bad);
	            		
	            		byte[] tempBuffer4=new byte[c4.length];
	                	for(int lv=0;lv<c4.length;lv++){
	                		tempBuffer4[lv]=(byte)(c4[lv]);
	                	}
	                	System.out.println("here");
	                	DatagramPacket sendPacket = new DatagramPacket(tempBuffer1, tempBuffer1.length, IPAddress, 7780);
	                    clientSocket.send(sendPacket);
	        	        
	                	}
	                    byteOutputStream.write(tempBuffer, 0, cnt);
	                	
	                }
	            }
	            /*
	             * close the buffer and socket streaming
	             */
	            bufferWritter.close();
	            byteOutputStream.close();
	        } catch (Exception e) {
	            System.out.println("CaptureThread::run()" + e);
	            System.exit(0);
	        }
	    }
	}

	class PlayThread extends Thread 
	{
		byte[] tempBuffer = new byte[10240];
		public void run() {
	        try {
	            int cnt;
	            /*
	             * to play back the audio recording, write command of socket sends the data bytes from buffer to speakers.
	             */
	            /*byte[] r = j.dec(tempBuffer);
	            while ((cnt = InputStream.read(r, 0, r.length)) != -1) {
	                if (cnt > 0) {
	                   sourceLine.write(r, 0, cnt);
	                }
	            }*/
	            while ((cnt = InputStream.read(tempBuffer, 0, tempBuffer.length)) != -1) {
	                if (cnt > 0) {
	                   sourceLine.write(tempBuffer, 0, cnt);
	                }
	            }
	            //                sourceLine.drain();
	            //             sourceLine.close();
	        } catch (Exception e) {
	            System.out.println(e);
	            System.exit(0);
	        }
	    }
	}
	class Delay extends Thread
	{
	    FileWriter fileWritter = null;
	    BufferedWriter bufferWritter = null;
	    PrintWriter pw;
	    DateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
	    File file =new File("delay.txt");
		public void run()
		{
			try{
				System.out.println("hello delay!");
				if(file.exists())
				{
					file.delete();
				}
				System.out.println("hello delete!");
				file.createNewFile();
				System.out.println("hello create!");
		        fileWritter = new FileWriter(file,true);
		    	bufferWritter = new BufferedWriter(fileWritter);
		    	pw = new PrintWriter(bufferWritter);
		    	FileReader fr_client = new FileReader("time_client.txt"); 
		    	BufferedReader br_client = new BufferedReader(fr_client); 
		    	FileReader fr_server = new FileReader("time_server.txt"); 
		    	BufferedReader br_server = new BufferedReader(fr_server); 
		    	System.out.println("hello append");
		    	String s_s,s_c;
		    	pw.append("server" + "\t-\t"+ "client" + "=\tdifference\n");
				while ((s_c = br_client.readLine())!=null && (s_s = br_server.readLine())!= null)
			    {
					long diff = 0;
					System.out.println("client line: " + s_c);
					System.out.println("server line: " + s_s);
					Date clientDate = sdf.parse(s_c);
					Date serverDate = sdf.parse(s_s);
					System.out.println("client-->"+clientDate.getTime());
					System.out.println("server-->"+serverDate.getTime());
					diff = (serverDate.getTime()-clientDate.getTime());
					System.out.println("diff-->"+ diff);
					pw.append(s_s + "\t" + s_c + "\t" + diff+"\n");
					System.out.println("hello writer");
			    }
				pw.close();
				
			}
			catch(IOException e)
			{
				e.printStackTrace();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    
		}
	}
}