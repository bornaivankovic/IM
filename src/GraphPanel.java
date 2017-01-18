import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JTextPane;
import javax.swing.JToolBar;

/**
 * Panel containing all the elements that are to be drawn
 *
 * @author Borna Ivankovic
 *
 */
public class GraphPanel extends JComponent {
	public static final int WIDE = 800;
	public static final int HIGH = 600;
	public static final int RADIUS = 35;
	public static final Random rnd = new Random();
	public ControlPanel control = new ControlPanel();
	public int radius = RADIUS;
	public List<Node> nodes = new ArrayList<>();
	public List<Node> selected = new ArrayList<>();
	public static List<Edge> edges = new ArrayList<>();
	public Point mousePt = new Point(WIDE / 2, HIGH / 2);
	public Rectangle mouseRect = new Rectangle();
	public boolean selecting = false;
	public JTextPane izlaz;
	public boolean cont = false;

	public GraphPanel() {
		setPreferredSize(new Dimension(800, 600));
		setLayout(new BorderLayout(0, 0));

		izlaz = new JTextPane();
		izlaz.setPreferredSize(new Dimension(6, 70));
		add(izlaz, BorderLayout.SOUTH);
		setOpaque(true);
		addMouseListener(new MouseHandler());
		addMouseMotionListener(new MouseMotionHandler());
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(WIDE, HIGH);
	}

	@Override
	public void paintComponent(Graphics g) {
		g.setColor(new Color(0x00f0f0f0));
		g.fillRect(0, 0, getWidth(), getHeight());
		for (Edge e : edges) {
			e.draw(g);
		}
		for (Node n : nodes) {
			n.draw(g);
		}
		if (selecting) {
			g.setColor(Color.darkGray);
			g.drawRect(mouseRect.x, mouseRect.y, mouseRect.width, mouseRect.height);
		}
	}

	private class MouseHandler extends MouseAdapter {

		@Override
		public void mouseReleased(MouseEvent e) {
			selecting = false;
			mouseRect.setBounds(0, 0, 0, 0);
			if (e.isPopupTrigger()) {
			}
			e.getComponent().repaint();
		}

		@Override
		public void mousePressed(MouseEvent e) {
			mousePt = e.getPoint();
			if (e.isShiftDown()) {
				Node.selectToggle(nodes, mousePt);
			} else if (Node.selectOne(nodes, mousePt)) {
				selecting = false;
			} else {
				Node.selectNone(nodes);
				selecting = true;
			}
			e.getComponent().repaint();
		}

	}

	private class MouseMotionHandler extends MouseMotionAdapter {

		Point delta = new Point();

		@Override
		public void mouseDragged(MouseEvent e) {
			if (selecting) {
				mouseRect.setBounds(Math.min(mousePt.x, e.getX()), Math.min(mousePt.y, e.getY()),
						Math.abs(mousePt.x - e.getX()), Math.abs(mousePt.y - e.getY()));
				Node.selectRect(nodes, mouseRect);
			} else {
				delta.setLocation(e.getX() - mousePt.x, e.getY() - mousePt.y);
				Node.updatePosition(nodes, delta);
				mousePt = e.getPoint();
			}
			e.getComponent().repaint();
		}
	}

	public JToolBar getControlPanel() {
		return control;
	}

	/**
	 * Custom toolbar class
	 *
	 * @author Borna Ivankovic
	 *
	 */
	public class ControlPanel extends JToolBar {

		public Action bellmanFord = new bfAction("Start");
		public Action cont = new ContinueAction("Continue");
		public JCheckBox koraci = new JCheckBox("Prikazi korake?");

		ControlPanel() {
			setLayout(new FlowLayout(FlowLayout.LEFT));
			setBackground(Color.lightGray);

			this.add(new JButton(bellmanFord));
			this.add(koraci);
			this.add(new JButton(cont));

		}
	}

	/**
	 * Action triggered upon clicking the continue button
	 *
	 * @author Borna Ivankovic
	 *
	 */
	private class ContinueAction extends AbstractAction {

		public ContinueAction(String name) {
			super(name);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			cont = true;
		}
	}

	/**
	 * Action triggered upon clicking the start button
	 *
	 * @author Borna Ivankovic
	 *
	 */
	private class bfAction extends AbstractAction {

		public bfAction(String string) {
			super(string);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			Thread t = new Thread(new BellmanFord());
			izlaz.setText("Starting Bellman-Ford");
			t.start();

			repaint();
		}

	}

	/**
	 * Class representing the Bellman-Ford algorithm.
	 *
	 * @author Borna Ivankovic
	 *
	 */
	private class BellmanFord implements Runnable {

		private HashMap<Node, Integer> udaljenosti = new HashMap<>();
		private HashMap<Node, Node> predhodnik = new HashMap<>();
		private final int INF = 10000;
		private Node start;

		/**
		 * All logic behind the algorithm is run here.
		 */
		@Override
		public void run() {
			Node.getSelected(nodes, selected);
			if (selected.size() == 1) {
				start = selected.get(0);
			} else {
				start = nodes.get(ThreadLocalRandom.current().nextInt(nodes.size()));
			}

			String startTxt = "Odabran pocetni cvor: " + start;
			izlaz.setText(startTxt);
			for (Node node : nodes) {
				udaljenosti.put(node, INF);
				predhodnik.put(node, null);
			}
			udaljenosti.replace(start, INF, 0);
			boolean imaPromjene = true;
			for (int i = 0; i < nodes.size() - 1; i++) {

				if (!imaPromjene) {
					break;
				}
				imaPromjene = false;
				for (Edge edge : edges) {
					izlaz.setText(startTxt + "\n" + "Udaljenosti: " + udaljenosti.toString() + "\n" + "Predhodnici: "
							+ predhodnik.toString());
					cont = !control.koraci.isSelected();
					edge.color = Color.BLUE;
					repaint();
					Node u = edge.n1;
					Node v = edge.n2;
					int w = edge.weight;
					if (udaljenosti.get(u) + w < udaljenosti.get(v)) {
						udaljenosti.replace(v, udaljenosti.get(u) + w);
						predhodnik.replace(v, u);
						imaPromjene = true;
					} else if (!edge.directivity && udaljenosti.get(v) + w < udaljenosti.get(u)) {
						udaljenosti.replace(u, udaljenosti.get(v) + w);
						predhodnik.replace(u, v);
						imaPromjene = true;
					}
					while (!cont) {
						try {
							Thread.sleep(10);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					edge.color = Color.DARK_GRAY;
				}
			}
			for (Edge edge : edges) {
				Node u = edge.n1;
				Node v = edge.n2;
				int w = edge.weight;
				if (udaljenosti.get(u) + w < udaljenosti.get(v)) {
					izlaz.setText("Otkriven ciklus negativne tezine");
				}
			}
			for (Node node : predhodnik.keySet()) {
				Node n2 = node;
				Node n1 = predhodnik.get(node);
				Edge e = new Edge();
				for (Edge edge : edges) {
					if (edge.n1.equals(n1) && edge.n2.equals(n2)
							|| !edge.directivity && edge.n2.equals(n1) && edge.n1.equals(n2))
						e = edge;
				}
				e.color = Color.GREEN;
			}
			repaint();
			izlaz.setText(izlaz.getText() + "\nBellman-Ford ended");
		}

	}
}