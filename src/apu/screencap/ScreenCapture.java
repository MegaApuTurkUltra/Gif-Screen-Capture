/**
 * 
 */
package apu.screencap;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.imageio.ImageWriteParam;
import javax.imageio.stream.FileImageOutputStream;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.MouseInputListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileSystemView;

/**
 * @author "MegaApuTurkUltra"
 * 
 */
public class ScreenCapture extends JFrame implements MouseInputListener,
		KeyListener {
	private static final long serialVersionUID = -857571183160518663L;
	final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
	JPanel p;
	Rectangle record;

	JMenuBar bar;
	File saveFile;
	boolean loop = true;
	boolean lowcolor = false;
	int time = 150;
	JFileChooser chooser;
	Properties props = new Properties();

	JButton start;

	Timer flash;

	JMenu stop, file, image, info;

	public ScreenCapture() {
		setAlwaysOnTop(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setTitle("Sup");
		setUndecorated(true);
		setBackground(new Color(100, 100, 100, 50));
		setLocationRelativeTo(null);
		addMouseListener(this);
		addKeyListener(this);
		addMouseMotionListener(this);
		((JComponent) getContentPane())
				.setBorder(new LineBorder(Color.BLACK, 2));
		p = new JPanel(new FlowLayout());
		p.setBorder(new EmptyBorder(50, 50, 50, 50));
		start = new JButton("File > Save to...");
		start.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				p.setVisible(false);
				getContentPane().setCursor(Cursor.getDefaultCursor());
				started = true;
				stop.setVisible(true);
				info.setText("< Click to stop");
				// ScreenCapture.this.setJMenuBar(null);
				ScreenCapture.this.setBackground(new Color(0, 0, 0, 0));
				record = new Rectangle(getX() + 2,
						getY() + 2 + bar.getHeight(), getWidth() - 4,
						getHeight() - 4 - bar.getHeight());
				// setSize(getWidth() + 4, getHeight() + 4 + bar.getHeight());
				// setLocation(getX() - 2, getY() - 2 - bar.getHeight());
				((JComponent) ScreenCapture.this.getContentPane())
						.setBorder(new LineBorder(Color.RED, 2));
				file.setVisible(false);
				image.setVisible(false);
				stop.setEnabled(true);
				try {
					CaptureThread.INSTANCE.createWriter(saveFile, time, loop,
							record, lowcolor);
					CaptureThread.INSTANCE.startCapture();
				} catch (Exception e1) {
					e1.printStackTrace();
					JOptionPane.showMessageDialog(ScreenCapture.this,
							"An error has occured: " + e1.getMessage(),
							"Error", JOptionPane.ERROR_MESSAGE);
					System.exit(0);
				}
				flash.start();
			}
		});
		start.setFont(start.getFont().deriveFont(30f));
		start.setEnabled(false);
		start.setOpaque(false);
		start.setCursor(Cursor.getDefaultCursor());
		p.add(start);
		JLabel label = new JLabel(
				"Drag me and resize me. This will be the capture area.");
		label.setBackground(UIManager.getColor("Panel.background"));
		label.setOpaque(true);
		label.setForeground(Color.BLUE);
		label.setFont(label.getFont().deriveFont(20f));
		p.add(label);
		p.setOpaque(false);
		p.setBackground(new Color(0, 0, 0, 0));
		((JComponent) getContentPane()).setOpaque(false);
		getContentPane().setBackground(new Color(0, 0, 0, 0));
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(p, BorderLayout.CENTER);

		try {
			FileInputStream in = new FileInputStream("sc.prefs");
			props.load(in);
			in.close();
		} catch (Exception e) {
		}

		chooser = new JFileChooser(props.getProperty("directory",
				FileSystemView.getFileSystemView().getDefaultDirectory()
						.getAbsolutePath()));
		chooser.setMultiSelectionEnabled(false);
		chooser.setFileHidingEnabled(false);
		chooser.setDialogTitle("Save file to:");
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setFileFilter(new FileFilter() {
			@Override
			public String getDescription() {
				return "GIF Files";
			}

			@Override
			public boolean accept(File f) {
				return f.getName().endsWith(".gif") || f.isDirectory();
			}
		});

		bar = new JMenuBar();

		file = new JMenu("File");
		JMenuItem save = new JMenuItem("Save to...");
		save.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (chooser.showOpenDialog(ScreenCapture.this) == JFileChooser.APPROVE_OPTION) {
					saveFile = chooser.getSelectedFile();
					props.setProperty("directory",
							chooser.getCurrentDirectory().getAbsolutePath());
					savePrefs();
					start.setText("Start");
					start.setEnabled(true);
				}
			}
		});
		file.add(save);
		JMenuItem exit = new JMenuItem("Exit");
		exit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		file.add(exit);
		bar.add(file);

		image = new JMenu("Image");
		JCheckBoxMenuItem lowcolor = new JCheckBoxMenuItem(
				"Convert to grayscale");
		lowcolor.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ScreenCapture.this.lowcolor = (((JCheckBoxMenuItem) e
						.getSource()).isSelected());
			}
		});
		image.add(lowcolor);
		JCheckBoxMenuItem loop = new JCheckBoxMenuItem("Loop", true);
		loop.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ScreenCapture.this.loop = (((JCheckBoxMenuItem) e.getSource())
						.isSelected());
			}
		});
		image.add(loop);
		JMenuItem speed = new JMenuItem("Frame Delay...");
		speed.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						String s = JOptionPane.showInputDialog(
								(Component) ScreenCapture.this,
								"Enter the frame delay in milliseconds");
						if (s != null) {
							try {
								if (s == "")
									throw new Exception();
								time = Integer.parseInt(s);
							} catch (Exception ex) {
								JOptionPane.showMessageDialog(
										ScreenCapture.this, "Enter a number",
										"Error", JOptionPane.ERROR_MESSAGE);
							}
						}
					}
				});
			}
		});
		image.add(speed);
		bar.add(image);

		stop = new JMenu("Stop Capture");
		stop.setEnabled(false);
		stop.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (!stop.isEnabled())
					return;
				stop.setSelected(false);
				stop.setText("Waiting for GIF to save...");
				stop.setEnabled(false);
				flash.stop();
				info.setVisible(false);
				new Thread(new Runnable() {
					@Override
					public void run() {
						CaptureThread.INSTANCE.stopCapture();
						stop.setText("Opening file...");
						try {
							Desktop.getDesktop().open(saveFile);
						} catch (IOException e1) {
							e1.printStackTrace();
						}
						System.exit(0);
					}
				}).start();
			}
		});
		stop.setVisible(false);
		bar.add(stop);
		info = new JMenu("File > Save to... to start");
		info.setEnabled(false);
		info.setFont(info.getFont().deriveFont(Font.ITALIC));
		bar.add(info);

		setJMenuBar(bar);
		bar.setCursor(Cursor.getDefaultCursor());

		setSize(500, 500 + bar.getHeight());
		setLocationRelativeTo(null);

		flash = new Timer(1000, new ActionListener() {
			boolean on = true;

			@Override
			public void actionPerformed(ActionEvent arg0) {
				on = !on;
				if (on) {
					((JComponent) ScreenCapture.this.getContentPane())
							.setBorder(new LineBorder(Color.RED, 2));
				} else {
					((JComponent) ScreenCapture.this.getContentPane())
							.setBorder(new LineBorder(Color.BLACK, 2));
				}
			}
		});
		flash.setCoalesce(true);
		flash.setRepeats(true);

		setVisible(true);
		System.out.println("Init done");
	}

	protected void savePrefs() {
		try {
			FileOutputStream out = new FileOutputStream("sc.prefs");
			System.out.println(new File("sc.prefs").getAbsolutePath());
			props.store(out, "Edit if you want, kinda useless");
			out.close();
		} catch (Exception e1) {
		}
	}

	public static ScreenCapture INSTANCE;

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
		}
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				INSTANCE = new ScreenCapture();
			}
		});
	}

	boolean pressed = false;
	boolean started = false;
	boolean onedge = false;
	int edge;
	int iwx, iwy;
	int ix, iy;
	int iw, ih;

	@Override
	public void mouseClicked(MouseEvent arg0) {

	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		pressed = true;
		iwx = arg0.getX();
		iwy = arg0.getY();
		ix = arg0.getXOnScreen();
		iy = arg0.getYOnScreen();
		iw = getWidth();
		ih = getHeight();
		if (iwx < 5) {
			onedge = true;
			edge = 1;
		} else if (iwy < 5) {
			onedge = true;
			edge = 4;
		} else if (iwy > getHeight() - 5) {
			onedge = true;
			edge = 2;
		} else if (iwx > getWidth() - 5) {
			onedge = true;
			edge = 3;
		}
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		pressed = false;
		onedge = false;
	}

	@Override
	public void mouseDragged(MouseEvent arg0) {
		initCaching();
		if (started)
			return;
		if (pressed && !onedge) {
			int x = arg0.getXOnScreen() - iwx, y = arg0.getYOnScreen() - iwy;
			setLocation(x, y, true);
		} else if (pressed && onedge) {
			if (edge == 3) {
				setSize(arg0.getXOnScreen() - (iw - iwx) - getX(true),
						getHeight(true), true);
			} else if (edge == 2) {
				setSize(getWidth(true), arg0.getYOnScreen() - (ih - iwy)
						- getY(true), true);
			} else if (edge == 1) {
				setLocation(arg0.getXOnScreen(), getY(true), true);
				setSize(iw + ix - arg0.getXOnScreen(), getHeight(true), true);
			} else if (edge == 4) {
				setLocation(getY(true), arg0.getYOnScreen(), true);
				setSize(getWidth(true), ih + iy - arg0.getYOnScreen(), true);
			}
		}
		if (getX(true) < 0)
			setLocation(0, getY(true), true);
		if (getY(true) < 0)
			setLocation(getX(true), 0, true);
		if (getX(true) + getWidth(true) > screen.width)
			setLocation(screen.width - getWidth(true), getY(true), true);
		if (getY(true) + getHeight(true) > screen.height)
			setLocation(getX(true), screen.height - getHeight(true), true);
		if (getWidth(true) > screen.width)
			setSize(screen.width, getHeight(true), true);
		if (getHeight(true) > screen.height)
			setSize(getWidth(true), screen.height, true);
		sendSizeChange();
		sendLocationChange();
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		if (started)
			return;
		if (e.getX() < 5) {
			getContentPane().setCursor(
					Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
		} else if (e.getY() < 5) {
			getContentPane().setCursor(
					Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
		} else if (e.getY() > getHeight() - 5) {
			getContentPane().setCursor(
					Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));
		} else if (e.getX() > getWidth() - 5) {
			getContentPane().setCursor(
					Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
		} else {
			getContentPane().setCursor(
					Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
		}
	}

	int tx, ty, tw, th;

	public void initCaching() {
		tx = getX();
		ty = getY();
		tw = getWidth();
		th = getHeight();
	}

	public void setSize(int w, int h, boolean wait) {
		tw = w;
		th = h;
	}

	public void setLocation(int x, int y, boolean wait) {
		tx = x;
		ty = y;
	}

	public int getWidth(boolean wait) {
		return tw;
	}

	public int getHeight(boolean wait) {
		return th;
	}

	public int getX(boolean wait) {
		return tx;
	}

	public int getY(boolean wait) {
		return ty;
	}

	public void sendSizeChange() {
		setSize(tw, th);
	}

	public void sendLocationChange() {
		setLocation(tx, ty);
	}

	static class WriterThread implements Runnable {
		static final WriterThread INSTANCE = new WriterThread();
		static final Thread THREAD = new Thread(INSTANCE);
		static final ConcurrentLinkedQueue<BufferedImage> images = new ConcurrentLinkedQueue<BufferedImage>();
		static boolean grayscale = false;
		static GifSequenceWriter writer;
		static final Object lock = new Object();

		static void setup(GifSequenceWriter writer2, boolean grayscale2) {
			writer = writer2;
			grayscale = grayscale2;
		}

		static void startWriter() {
			if (!THREAD.isAlive()) {
				THREAD.start();
			}
		}

		static void stopWriter() {
			while (THREAD.isAlive()) {
				THREAD.interrupt();
				synchronized (lock) {
					try {
						lock.wait(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}

		@Override
		public void run() {
			while (!Thread.interrupted()) {
				while (images.size() > 0) {
					BufferedImage image = images.poll();
					if (grayscale) {
						BufferedImage img2 = new BufferedImage(
								image.getWidth(), image.getHeight(),
								BufferedImage.TYPE_BYTE_GRAY);
						Graphics2D g = img2.createGraphics();
						g.drawImage(image, 0, 0, null);
						g.dispose();
						image = img2;
					}
					try {
						writer.writeToSequence(image);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				Thread.yield();
			}
			try {
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			synchronized (lock) {
				lock.notify();
			}
		}
	}

	static class CaptureThread implements Runnable {
		static final CaptureThread INSTANCE = new CaptureThread();
		static final Thread THREAD = new Thread(INSTANCE);
		GifSequenceWriter writer;
		Rectangle bounds;
		int x, y, w, h;
		Robot rt;
		int time;
		boolean lowcolor;

		public void createWriter(File file, int time, boolean loop,
				Rectangle bounds, boolean lc) throws IOException, AWTException {
			lowcolor = lc;
			writer = new GifSequenceWriter(new FileImageOutputStream(file),
					BufferedImage.TYPE_INT_ARGB, time, loop);
			writer.imageWriteParam
					.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
			writer.imageWriteParam.setCompressionType("LZW");
			this.bounds = bounds;
			this.x = bounds.x;
			this.y = bounds.y;
			this.w = bounds.width;
			this.h = bounds.height;
			this.time = time;
			rt = new Robot();

			WriterThread.setup(writer, lowcolor);
		}

		public void startCapture() {
			if (writer == null)
				throw new IllegalStateException("Call createWriter first!");
			THREAD.start();
			WriterThread.startWriter();
		}

		public void stopCapture() {
			if (!THREAD.isAlive())
				throw new IllegalStateException("Call startCapture first!");
			THREAD.interrupt();
			WriterThread.stopWriter();
		}

		@Override
		public void run() {
			while (true) {
				long start = System.currentTimeMillis();
				WriterThread.images.add(rt.createScreenCapture(bounds));
				long stop = System.currentTimeMillis();
				if (stop - start > time) {
					if (Thread.interrupted())
						break;
					ScreenCapture.INSTANCE.info
							.setText("WARNING: Can't keep up! Increase the frame delay...");
					ScreenCapture.INSTANCE.info.setForeground(Color.RED);
					continue;
				}
				try {
					Thread.sleep(time - (stop - start));
				} catch (InterruptedException e) {
					break;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		if (arg0.getKeyCode() == KeyEvent.VK_S) {
			for (ActionListener a : stop.getActionListeners()) {
				a.actionPerformed(null);
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
	}
}
