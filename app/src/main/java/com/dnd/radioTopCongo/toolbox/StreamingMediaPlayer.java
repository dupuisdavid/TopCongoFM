package com.dnd.radioTopCongo.toolbox;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * MediaPlayer does not yet support streaming from external URLs so this class provides a pseudo-streaming function
 * by downloading the content incrementally & playing as soon as we get enough audio in our temporary storage.
 */
public class StreamingMediaPlayer {

	// Assume 96kbps*10secs/8bits per byte
    private static final int INTIAL_KB_BUFFER = 96*10/8;

	private TextView textStreamed;
	
	private ImageButton playButton;
	
	private ProgressBar	progressBar;
	
	//  Track for display by progressBar
	private long mediaLengthInKb, mediaLengthInSeconds;
	private int totalKbRead = 0;
	
	// Create Handler to call View updates on the main UI thread.
	private final Handler handler = new Handler();

	private MediaPlayer mediaPlayer;
	
	private File downloadingMediaFile; 
	
	private boolean isInterrupted;
	
	private Context context;
	
	private int counter = 0;
	
 	public StreamingMediaPlayer(Context  context,TextView textStreamed, ImageButton	playButton, ProgressBar	progressBar) {
 		this.context = context;
		this.textStreamed = textStreamed;
		this.playButton = playButton;
		this.progressBar = progressBar;
	}
	
    /**  
     * Progressivly download the media to a temporary location and update the MediaPlayer as new content becomes available.
     */  
    public void startStreaming(final String mediaUrl, long mediaLengthInKb, long mediaLengthInSeconds) throws FileNotFoundException, IOException {
    	
    	this.mediaLengthInKb = mediaLengthInKb;
    	this.mediaLengthInSeconds = mediaLengthInSeconds;
    	
		Runnable r = new Runnable() {   
	        public void run() {   
	            try {   
	        		downloadAudioIncrement(mediaUrl);
	            } catch (FileNotFoundException e) {
	            	Log.e(getClass().getName(), "Unable to initialize the MediaPlayer for fileUrl=" + mediaUrl + " (FileNotFoundException)", e);
	            	return;
	            } catch (IOException e) {
	            	Log.e(getClass().getName(), "Unable to initialize the MediaPlayer for fileUrl=" + mediaUrl + " (IOException)", e);
	            	return;
	            }   
	        }   
	    };
	    
	    new Thread(r).start();
    }
    
    /**  
     * Download the URL stream to a temporary location and then call the setDataSource  
     * for that local file
     */  
    public void downloadAudioIncrement(String mediaURL) throws FileNotFoundException, IOException {
    	
//    	URLConnection cn = new URL(mediaURL).openConnection();
//      cn.connect();
    	
    	// HELP
    	// FileNotFoundException for HttpURLConnection in Ice Cream Sandwich
    	// http://stackoverflow.com/questions/9365829/filenotfoundexception-for-httpurlconnection-in-ice-cream-sandwich
    	// 4.2.1 (Ice Cream Sandwich) GoDaddy hosted website: (httpurlconnection) URL works on WiFi, but throws Java.IO.FileNotFoundException on 3G
    	// http://stackoverflow.com/questions/21241316/4-2-1-ice-cream-sandwich-godaddy-hosted-website-httpurlconnection-url-works?rq=1
    	
    	URL url = new URL(mediaURL);
    	HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    	connection.setRequestMethod("GET");
//    	connection.setInstanceFollowRedirects(false);
//    	connection.setDoInput(true);
//    	connection.setDoOutput(false);
    	connection.connect();
    	
    	
    	Log.i("connection.getResponseCode() : ", "" + connection.getResponseCode());
    	Log.i("connection.getResponseMessage() : ", "" + connection.getResponseMessage());
        

        InputStream stream = connection.getInputStream();
        
        
        if (stream == null) {
        	Log.e(getClass().getName(), "Unable to create InputStream for mediaUrl:" + mediaURL);
        }
        
		downloadingMediaFile = new File(context.getCacheDir(), "downloadingMedia.dat");
		
		// Just in case a prior deletion failed because our code crashed or something, we also delete any previously 
		// downloaded file to ensure we start fresh.  If you use this code, always delete 
		// no longer used downloads else you'll quickly fill up your hard disk memory.  Of course, you can also 
		// store any previously downloaded file in a separate data cache for instant replay if you wanted as well.
		if (downloadingMediaFile.exists()) {
			downloadingMediaFile.delete();
		}

        FileOutputStream out = new FileOutputStream(downloadingMediaFile);   
        byte buf[] = new byte[16384];
        int totalBytesRead = 0;
        int incrementalBytesRead = 0;
        do {
        	int numread = stream.read(buf);
        	
            if (numread <= 0) {
                break;
            }
            
            out.write(buf, 0, numread);
            totalBytesRead += numread;
            // CODE UPDATE BY DDUPUIS - 2014-03-09
            incrementalBytesRead = incrementalBytesRead + numread;
            totalKbRead = totalBytesRead/1000;
            
            testMediaBuffer();
           	fireDataLoadUpdate();
           	
           	
        } while (validateNotInterrupted());  
        
       	stream.close();
       	
       	// CODE ADD BY DDUPUIS - 2014-03-09
 		if (out != null) {
 			out.close();
 		}
       	
        if (validateNotInterrupted()) {
	       	fireDataFullyLoaded();
        }
    }  

    private boolean validateNotInterrupted() {
		if (isInterrupted) {
			if (mediaPlayer != null) {
				mediaPlayer.pause();
				//mediaPlayer.release();
			}
			return false;
		} else {
			return true;
		}
    }

    /**
     * Test whether we need to transfer buffered data to the MediaPlayer.
     * Interacting with MediaPlayer on non-main UI thread can causes crashes to so perform this using a Handler.
     */  
    private void testMediaBuffer() {
	    Runnable updater = new Runnable() {
	        public void run() {
	            if (mediaPlayer == null) {
	            	//  Only create the MediaPlayer once we have the minimum buffered data
	            	if ( totalKbRead >= INTIAL_KB_BUFFER) {
	            		try {
		            		startMediaPlayer();
	            		} catch (Exception e) {
	            			Log.e(getClass().getName(), "Error copying buffered conent.", e);    			
	            		}
	            	}
	            } else if ( mediaPlayer.getDuration() - mediaPlayer.getCurrentPosition() <= 1000 ){ 
	            	//  NOTE:  The media player has stopped at the end so transfer any existing buffered data
	            	//  We test for < 1second of data because the media player can stop when there is still
	            	//  a few milliseconds of data left to play
	            	transferBufferToMediaPlayer();
	            }
	        }
	    };
	    handler.post(updater);
    }
    
    private void startMediaPlayer() {
        try {
        	
        	File bufferedFile = new File(context.getCacheDir(), "playingMedia" + (counter++) + ".dat");
        	
        	// We double buffer the data to avoid potential read/write errors that could happen if the 
        	// download thread attempted to write at the same time the MediaPlayer was trying to read.
        	// For example, we can't guarantee that the MediaPlayer won't open a file for playing and leave it locked while 
        	// the media is playing.  This would permanently deadlock the file download.  To avoid such a deadloack, 
        	// we move the currently loaded data to a temporary buffer file that we start playing while the remaining 
        	// data downloads.  
        	moveFile(downloadingMediaFile,bufferedFile);
    		
        	Log.i(getClass().getName(), "Buffered File path: " + bufferedFile.getAbsolutePath());
        	Log.i(getClass().getName(), "Buffered File length: " + bufferedFile.length() + "");
        	
        	mediaPlayer = createMediaPlayer(bufferedFile);
        	
    		// We have pre-loaded enough content and started the MediaPlayer so update the buttons & progress meters.
	    	mediaPlayer.start();
	    	startPlayProgressUpdater();        	
			playButton.setEnabled(true);
			
        } catch (IOException e) {
        	Log.e(getClass().getName(), "Error initializing the MediaPlayer.", e);
        	return;
        }   
    }
    
    private MediaPlayer createMediaPlayer(File mediaFile) throws IOException {
    	
    	MediaPlayer mPlayer = new MediaPlayer();
    	
    	// ON_PREPARE_LISTENER
    	mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
			@Override
			public void onPrepared(MediaPlayer mp) {
				Log.i(getClass().getName(), "MediaPlayer onPrepared");
			}
		});
    	
    	// ON_BUFFERING_LISTENER
    	mPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
			@Override
			public void onBufferingUpdate(MediaPlayer mp, int percent) {
				Log.i(getClass().getName(), "MediaPlayer onBufferingUpdate: (" + percent + ")");
			}
		});
    	
    	// ON_COMPLETION_LISTENER
    	mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				Log.i(getClass().getName(), "MediaPlayer onCompletion");
			}
		});
    	
    	// ON_INFO_LISTENER
    	mPlayer.setOnInfoListener(new MediaPlayer.OnInfoListener() {
			@Override
			public boolean onInfo(MediaPlayer mp, int what, int extra) {
				Log.i(getClass().getName(), "MediaPlayer onInfo: (" + what +") with extra (" +extra +")");
		        
//		     	MEDIA_INFO_UNKNOWN
//		      	MEDIA_INFO_VIDEO_TRACK_LAGGING
//		      	MEDIA_INFO_VIDEO_RENDERING_START
//		      	MEDIA_INFO_BUFFERING_START
//		      	MEDIA_INFO_BUFFERING_END
//		      	MEDIA_INFO_BAD_INTERLEAVING
//		      	MEDIA_INFO_NOT_SEEKABLE
//		      	MEDIA_INFO_METADATA_UPDATE
//		      	MEDIA_INFO_UNSUPPORTED_SUBTITLE
//		      	MEDIA_INFO_SUBTITLE_TIMED_OUT

		        switch (what) {
		        
		        	// MediaPlayer.MEDIA_INFO_UNKNOWN
		        	case 1:
		        		
		        		break;
		        		
			        // MediaPlayer.MEDIA_INFO_BUFFERING_START
			        case 701:
			        	
			            break;
			            
			        // MediaPlayer.MEDIA_INFO_BUFFERING_END
			        case 702: 
			        	
			            break;
			            
			        // MediaPlayer.MEDIA_INFO_NETWORK_BANDWIDTH
			        case 703: 
			        	
			            break;
			            
			        // MediaPlayer.MEDIA_INFO_METADATA_UPDATE
			        case 802: 
			        	
			        	break;
			
			        default:
			            break;
		        }

		        return false;
			}
		});
    	
    	// ON_ERROR_LISTENER
    	mPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
	        public boolean onError(MediaPlayer mp, int what, int extra) {
	        	Log.e(getClass().getName(), "Error in MediaPlayer: (" + what +") with extra (" +extra +")");
	        	
//	          	MEDIA_ERROR_IO
//	          	MEDIA_ERROR_MALFORMED
//	          	MEDIA_ERROR_UNSUPPORTED
//	          	MEDIA_ERROR_TIMED_OUT   

	            switch (extra) {
	            
	            	// MEDIA_ERROR_IO
	            	case -1004:
	            		
	            		break;
	            		
	    	        // MEDIA_ERROR_MALFORMED
	    	        case -1007:
	    	        	
	    	            break;
	    	            
	    	        // MEDIA_ERROR_UNSUPPORTED
	    	        case -1010:
	    	        	
	    	            break;
	    	            
	    	        // MEDIA_ERROR_TIMED_OUT
	    	        case -110:
	    	        	
	    	            break;
	    	
	    	        default:
	    	            break;
	            }
	            
	    		return false;
	        }
	    });

		//  It appears that for security/permission reasons, it is better to pass a FileDescriptor rather than a direct path to the File.
		//  Also I have seen errors such as "PVMFErrNotSupported" and "Prepare failed.: status=0x1" if a file path String is passed to
		//  setDataSource().  So unless otherwise noted, we use a FileDescriptor here.
		FileInputStream fileInputStream = new FileInputStream(mediaFile);
		mPlayer.setDataSource(fileInputStream.getFD());
		mPlayer.prepare();
		
		// CODE ADD BY DDUPUIS - 2014-03-09
		if (fileInputStream != null) {
			fileInputStream.close();
		}
		
		
		return mPlayer;
    }
    
    /**
     * Transfer buffered data to the MediaPlayer.
     * NOTE: Interacting with a MediaPlayer on a non-main UI thread can cause thread-lock and crashes so 
     * this method should always be called using a Handler.
     */  
    private void transferBufferToMediaPlayer() {
	    try {
	    	// First determine if we need to restart the player after transferring data...e.g. perhaps the user pressed pause
	    	boolean wasPlaying = mediaPlayer.isPlaying();
	    	int curPosition = mediaPlayer.getCurrentPosition();
	    	
	    	// Copy the currently downloaded content to a new buffered File.  Store the old File for deleting later. 
	    	File oldBufferedFile = new File(context.getCacheDir(),"playingMedia" + counter + ".dat");
	    	File bufferedFile = new File(context.getCacheDir(),"playingMedia" + (counter++) + ".dat");

	    	//  This may be the last buffered File so ask that it be delete on exit.  If it's already deleted, then this won't mean anything.  If you want to 
	    	// keep and track fully downloaded files for later use, write caching code and please send me a copy.
	    	bufferedFile.deleteOnExit();   
	    	moveFile(downloadingMediaFile, bufferedFile);

	    	// Pause the current player now as we are about to create and start a new one.  So far (Android v1.5),
	    	// this always happens so quickly that the user never realized we've stopped the player and started a new one
	    	mediaPlayer.pause();

	    	// Create a new MediaPlayer rather than try to re-prepare the prior one.
        	mediaPlayer = createMediaPlayer(bufferedFile);
    		mediaPlayer.seekTo(curPosition);
    		
    		//  Restart if at end of prior buffered content or mediaPlayer was previously playing.  
    		//	NOTE:  We test for < 1second of data because the media player can stop when there is still
        	//  a few milliseconds of data left to play
    		boolean atEndOfFile = mediaPlayer.getDuration() - mediaPlayer.getCurrentPosition() <= 1000;
        	if (wasPlaying || atEndOfFile){
        		mediaPlayer.start();
        	}

	    	// Lastly delete the previously playing buffered File as it's no longer needed.
	    	oldBufferedFile.delete();
	    	
	    } catch (Exception e) {
	    	Log.e(getClass().getName(), "Error updating to newly loaded content.", e);            		
		}
    }
    
    private void fireDataLoadUpdate() {
		Runnable updater = new Runnable() {
	        public void run() {
	        	textStreamed.setText((totalKbRead + " Kb read"));
	    		float loadProgress = ((float)totalKbRead/(float)mediaLengthInKb);
	    		progressBar.setSecondaryProgress((int)(loadProgress*100));
	        }
	    };
	    handler.post(updater);
    }
    
    private void fireDataFullyLoaded() {
		Runnable updater = new Runnable() { 
			public void run() {
   	        	transferBufferToMediaPlayer();

   	        	// Delete the downloaded File as it's now been transferred to the currently playing buffer file.
   	        	downloadingMediaFile.delete();
	        	textStreamed.setText(("Audio full loaded: " + totalKbRead + " Kb read"));
	        }
	    };
	    handler.post(updater);
    }
    
    public MediaPlayer getMediaPlayer() {
    	return mediaPlayer;
	}
	
    public void startPlayProgressUpdater() {
    	float progress = (((float)mediaPlayer.getCurrentPosition()/1000)/mediaLengthInSeconds);
    	progressBar.setProgress((int)(progress*100));
    	
		if (mediaPlayer.isPlaying()) {
			Runnable notification = new Runnable() {
		        public void run() {
		        	startPlayProgressUpdater();
				}
		    };
		    handler.postDelayed(notification,1000);
    	}
    }    
    
    public void interrupt() {
    	playButton.setEnabled(false);
    	isInterrupted = true;
    	validateNotInterrupted();
    }
    
    /**
     *  Move the file in oldLocation to newLocation.
     */
	public void moveFile(File oldLocation, File newLocation) throws IOException {

		if (oldLocation.exists()) {
			BufferedInputStream reader = new BufferedInputStream( new FileInputStream(oldLocation) );
			BufferedOutputStream writer = new BufferedOutputStream( new FileOutputStream(newLocation, false));
            try {
		        byte[]  buff = new byte[8192];
		        int numChars;
		        while ( (numChars = reader.read(  buff, 0, buff.length ) ) != -1) {
		        	writer.write( buff, 0, numChars );
      		    }
            } catch( IOException ex ) {
				throw new IOException("IOException when transferring " + oldLocation.getPath() + " to " + newLocation.getPath());
            } finally {
                try {
                    if ( reader != null ){                    	
                    	writer.close();
                        reader.close();
                    }
                } catch( IOException ex ){
				    Log.e(getClass().getName(),"Error closing files when transferring " + oldLocation.getPath() + " to " + newLocation.getPath() ); 
				}
            }
        } else {
			throw new IOException("Old location does not exist when transferring " + oldLocation.getPath() + " to " + newLocation.getPath() );
        }
	}
}
