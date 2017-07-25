import java.awt.EventQueue;
import java.awt.BorderLayout;
import java.awt.FlowLayout;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

import java.awt.TextField;
import java.awt.TextArea;
import java.awt.Label;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.InputStream;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.TransferHandler;
import javax.swing.SwingUtilities;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JScrollBar;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MovieConvGUI extends OutputStream {
	private JFrame frame;
    static private JTextArea _area;
    private ByteArrayOutputStream _buf;
  	 private static final String window_title = "動画変換";
	static TextField tf01 = new TextField(" -r 29.97 -s 1920x1080 -vb 1024k -ab 24k ",80);
	static Label l1 = new Label("オプション:");
	
	
    public MovieConvGUI(JTextArea area) {
    	
    	_area = area;
        _buf = new ByteArrayOutputStream();
    	// ドロップ操作を有効にする
		_area.setTransferHandler(new DropFileHandler());

    }
    
    @Override
    public void write(int b) throws IOException {
        _buf.write(b);
    }
    
    @Override
    public void flush() throws IOException {

        // Swing のイベントスレッドにのせる
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                _area.append(_buf.toString());
                _area.setCaretPosition(_area.getText().length());
            	_buf.reset();
            	
            }
        });
    }


	
		
	/**
		 * エンコードする
		 */
		
		static private Thread errRun=null;  
		//static private InputStream stream;
		//static private BufferedReader br;
		Process p;
		boolean encodeData(String in) throws IOException{
			
			System.out.println("\nencode....\n");
			String out=in.replace(".","[encoded].");
			String opt=tf01.getText(); //tf01.get" -r 29.97 -s 1920x1080 -vb 1024k -ab 24k ";
		 
		  Runtime rt = Runtime.getRuntime();
  		  p=rt.exec("ffmpeg -y -i " +"\""+in+"\""+opt+"\""+out+"\"");
		  
		  InputStream stream = p.getErrorStream();
		
		 try {
		 	Runnable errStreamThread = new Runnable(){
		 	
		 	public void run(){
		 	try{
  		 	//System.out.println("\nencode....\n");
		 				
  		 String line; 
  		 BufferedReader br = new BufferedReader(new InputStreamReader(stream));
  		 while (true) {
			
  		 	line = br.readLine();
  		 	
  		 	if (line == null) {
  		 			System.out.println("end...");
  		 			break;
                }
  		 System.out.println(line);
  		 }
		 				
	    } catch (IOException ex) {
    	  ex.printStackTrace();
    	 
	    }
	}
	};
		
   		 errRun = new Thread(errStreamThread);
		 errRun.start();
		
	
		 //
		 	
		 }catch(Exception e){
		 
		 }finally{
		 //if(br!=null)br.close();
		 if(stream==null)
		 	{
		 		//stream.close();
		 		p.destroy();
		 	}
		
		 }
			
		
		return true;

	}		
		
	
	/**
	 * ドロップ操作の処理を行うクラス
	 */
	List<File> files;
	
	private class DropFileHandler extends TransferHandler {
 	
		/**
		 * ドロップされたものを受け取るか判断 (ファイルのときだけ受け取る)
		 */
		@Override
		public boolean canImport(TransferSupport support) {
						
			if (!support.isDrop()) {
				// ドロップ操作でない場合は受け取らない
		        return false;
		    }
 
			if (!support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
				// ドロップされたのがファイルでない場合は受け取らない
		        return false;
			}
			
			return true;
		}
	
 
		/**
		 * ドロップされたファイルを受け取る
		 */
		StringBuffer fileList;
		@Override
		public boolean importData(TransferSupport support) {
			// 受け取っていいものか確認する
			if (!canImport(support)) {
		        return false;
		    }
			// ドロップ処理
			Transferable t = support.getTransferable();
			try {
				// ファイルを受け取る
				
				files = (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
 
				// テキストエリアに表示するファイル名リストを作成する
				fileList = new StringBuffer();
				// テキストエリアにファイル名のリストを表示する
				
				for (File file : files){
					
					fileList.append(file.getPath());
					System.out.println(file.getPath());
					
						
						encodeData(file.getPath());
						//	try{
						//	p.waitFor();
						//	}catch(InterruptedException e){}
						
					fileList.append("\n");

				}

				
			} catch (UnsupportedFlavorException | IOException e) {
				e.printStackTrace();
			}
			return true;
		}

	}
		
	
    public static void main(String[] args) {
	
    	
       	EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
		
					

		JScrollPane scrollPane = new JScrollPane();
    	JTextArea area = new JTextArea();
        //area.setEditable(false);  // ReadOnly に
        MovieConvGUI stream = new MovieConvGUI(area);
        System.setOut(new PrintStream(stream, true));    // true は AutoFlush の設定

        JFrame frame = new JFrame();
		frame.setTitle(MovieConvGUI.window_title);

		JPanel p1 = new JPanel();			
					
		//p1.setLayout(new FlowLayout(FlowLayout.LEFT));
    	p1.add(l1);
		p1.add(tf01);
			
		frame.getContentPane().add(area);			
		scrollPane.setViewportView(area);
		
		frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
		area.append("ここに入力データをドラッグ\n");
		frame.getContentPane().add(p1, BorderLayout.PAGE_START);
					
    	frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        //frame.setLocationRelativeTo(0,0);
        //frame.setSize(1024,500);
        frame.setBounds(0, 0, 1024, 500);
		frame.setVisible(true);

		JScrollBar scrollBar = scrollPane.getVerticalScrollBar();
		scrollBar.setValue(scrollBar.getMaximum());
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
    	
        }

}

	
