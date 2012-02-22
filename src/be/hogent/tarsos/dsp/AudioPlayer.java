/**
*
*  TarsosDSP is developed by Joren Six at 
*  The Royal Academy of Fine Arts & Royal Conservatory,
*  University College Ghent,
*  Hoogpoort 64, 9000 Ghent - Belgium
*  
*  http://tarsos.0110.be/tag/TarsosDSP
*
**/
package be.hogent.tarsos.dsp;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

/**
 * This AudioProcessor can be used to sync events with sound. It uses a pattern
 * described in JavaFX Special Effects Taking Java RIA to the Extreme with
 * Animation, Multimedia, and Game Element Chapter 9 page 185: <blockquote><i>
 * The variable line is the Java Sound object that actually makes the sound. The
 * write method on line is interesting because it blocks until it is ready for
 * more data. </i></blockquote> If this AudioProcessor chained with other
 * AudioProcessors the others should be able to operate in real time or process
 * the signal on a separate thread.
 * 
 * @author Joren Six
 */
public final class AudioPlayer implements AudioProcessor {
	

	/**
	 * The line to send sound to. Is also used to keep everything in sync.
	 */
	private SourceDataLine line;

	
	private final AudioFormat format;

	/**
	 * Creates a new audio player.
	 * 
	 * @param format
	 *            The AudioFormat of the buffer.
	 * @throws LineUnavailableException
	 *             If no output line is available.
	 */
	public AudioPlayer(final AudioFormat format)	throws LineUnavailableException {
		final DataLine.Info info = new DataLine.Info(SourceDataLine.class,format);
		this.format = format;
		line = (SourceDataLine) AudioSystem.getLine(info);
		line.open();
		line.start();	
	}
	
	@Override
	public boolean process(AudioEvent audioEvent) {
		// overlap in samples * nr of bytes / sample = bytes overlap
		int byteOverlap = audioEvent.getOverlap() * format.getFrameSize();
		int byteStepSize = audioEvent.getBufferSize() * format.getFrameSize() - byteOverlap;
		
		// Play only the audio that has not been played already.
		line.write(audioEvent.getByteBuffer(), byteOverlap, byteStepSize);
		return true;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see be.hogent.tarsos.util.RealTimeAudioProcessor.AudioProcessor#
	 * processingFinished()
	 */
	public void processingFinished() {
		// cleanup
		line.drain();
		line.close();
	}
}