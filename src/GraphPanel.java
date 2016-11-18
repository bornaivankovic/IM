import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
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
import java.util.ListIterator;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSpinner;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


public class GraphPanel extends JComponent {

    public static final int WIDE = 800;
    public static final int HIGH = 600;
    public static final int RADIUS = 35;
    public static final Random rnd = new Random();
    public ControlPanel control = new ControlPanel();
    public int radius = RADIUS;
    public List<Node> nodes = new ArrayList<Node>();
    public List<Node> selected = new ArrayList<Node>();
    public List<Edge> edges = new ArrayList<Edge>();
    public Point mousePt = new Point(WIDE / 2, HIGH / 2);
    public Rectangle mouseRect = new Rectangle();
    public boolean selecting = false;
    public JTextPane izlaz;
    public boolean cont=false;

    public GraphPanel() {
    	setPreferredSize(new Dimension(800, 600));
    	setLayout(new BorderLayout(0, 0));
    	
    	izlaz = new JTextPane();
    	izlaz.setPreferredSize(new Dimension(6, 70));
    	add(izlaz, BorderLayout.SOUTH);
        this.setOpaque(true);
        this.addMouseListener(new MouseHandler());
        this.addMouseMotionListener(new MouseMotionHandler());
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
            g.drawRect(mouseRect.x, mouseRect.y,
                mouseRect.width, mouseRect.height);
        }
    }

    private class MouseHandler extends MouseAdapter {

        @Override
        public void mouseReleased(MouseEvent e) {
            selecting = false;
            mouseRect.setBounds(0, 0, 0, 0);
            if (e.isPopupTrigger()) {
                showPopup(e);
            }
            e.getComponent().repaint();
        }

        @Override
        public void mousePressed(MouseEvent e) {
            mousePt = e.getPoint();
            if (e.isShiftDown()) {
                Node.selectToggle(nodes, mousePt);
            } else if (e.isPopupTrigger()) {
                Node.selectOne(nodes, mousePt);
                showPopup(e);
            } else if (Node.selectOne(nodes, mousePt)) {
                selecting = false;
            } else {
                Node.selectNone(nodes);
                selecting = true;
            }
            e.getComponent().repaint();
        }

        private void showPopup(MouseEvent e) {
            control.popup.show(e.getComponent(), e.getX(), e.getY());
        }
    }

    private class MouseMotionHandler extends MouseMotionAdapter {

        Point delta = new Point();

        @Override
        public void mouseDragged(MouseEvent e) {
            if (selecting) {
                mouseRect.setBounds(
                    Math.min(mousePt.x, e.getX()),
                    Math.min(mousePt.y, e.getY()),
                    Math.abs(mousePt.x - e.getX()),
                    Math.abs(mousePt.y - e.getY()));
                Node.selectRect(nodes, mouseRect);
            } else {
                delta.setLocation(
                    e.getX() - mousePt.x,
                    e.getY() - mousePt.y);
                Node.updatePosition(nodes, delta);
                mousePt = e.getPoint();
            }
            e.getComponent().repaint();
        }
    }

    public JToolBar getControlPanel() {
        return control;
    }

    public class ControlPanel extends JToolBar {

    	public Action newNode = new NewNodeAction("New");
    	public Action clearAll = new ClearAction("Clear");
    	public Action color = new ColorAction("Color");
    	public Action connect = new ConnectAction("Connect");
    	public Action delete = new DeleteAction("Delete");
    	public Action random = new RandomAction("Random");
    	public Action bellmanFord = new bfAction("Bellman-Ford");
    	public Action cont = new ContinueAction("Continue");
    	public JButton defaultButton = new JButton(newNode);
    	public JCheckBox koraci = new JCheckBox("Prikazi korake?");
    	public ColorIcon hueIcon = new ColorIcon(Color.blue);
    	public JPopupMenu popup = new JPopupMenu();

        ControlPanel() {
            this.setLayout(new FlowLayout(FlowLayout.LEFT));
            this.setBackground(Color.lightGray);

            this.add(defaultButton);
            this.add(new JButton(clearAll));
            this.add(new JButton(color));
            this.add(new JLabel(hueIcon));
            JSpinner js = new JSpinner();
            js.setModel(new SpinnerNumberModel(RADIUS, 5, 100, 5));
            js.addChangeListener(new ChangeListener() {

                @Override
                public void stateChanged(ChangeEvent e) {
                    JSpinner s = (JSpinner) e.getSource();
                    radius = (Integer) s.getValue();
                    Node.updateRadius(nodes, radius);
                    GraphPanel.this.repaint();
                }
            });
            this.add(new JLabel("Size:"));
            this.add(js);
            this.add(new JButton(random));
            this.add(new JButton(bellmanFord));
            this.add(koraci);
            this.add(new JButton(cont));

            popup.add(new JMenuItem(newNode));
            popup.add(new JMenuItem(color));
            popup.add(new JMenuItem(connect));
            popup.add(new JMenuItem(delete));
        }
    }

        

    private class ClearAction extends AbstractAction {

        public ClearAction(String name) {
            super(name);
        }

        public void actionPerformed(ActionEvent e) {
            nodes.clear();
            edges.clear();
            repaint();
        }
    }

    private class ColorAction extends AbstractAction {

        public ColorAction(String name) {
            super(name);
        }

        public void actionPerformed(ActionEvent e) {
            Color color = control.hueIcon.getColor();
            color = JColorChooser.showDialog(
                GraphPanel.this, "Choose a color", color);
            if (color != null) {
                Node.updateColor(nodes, color);
                control.hueIcon.setColor(color);
                control.repaint();
                repaint();
            }
        }
    }

    private class ConnectAction extends AbstractAction {

        public ConnectAction(String name) {
            super(name);
        }

        public void actionPerformed(ActionEvent e) {
            Node.getSelected(nodes, selected);
            if (selected.size() > 1) {
                for (int i = 0; i < selected.size() - 1; ++i) {
                    Node n1 = selected.get(i);
                    Node n2 = selected.get(i + 1);
                    edges.add(new Edge(n1, n2,false,10));
                }
            }
            repaint();
        }
    }

    private class DeleteAction extends AbstractAction {

        public DeleteAction(String name) {
            super(name);
        }

        public void actionPerformed(ActionEvent e) {
            ListIterator<Node> iter = nodes.listIterator();
            while (iter.hasNext()) {
                Node n = iter.next();
                if (n.isSelected()) {
                    deleteEdges(n);
                    iter.remove();
                }
            }
            repaint();
        }

        private void deleteEdges(Node n) {
            ListIterator<Edge> iter = edges.listIterator();
            while (iter.hasNext()) {
                Edge e = iter.next();
                if (e.n1 == n || e.n2 == n) {
                    iter.remove();
                }
            }
        }
    }


    private class NewNodeAction extends AbstractAction {

        public NewNodeAction(String name) {
            super(name);
        }

        public void actionPerformed(ActionEvent e) {
            Node.selectNone(nodes);
            Point p = mousePt.getLocation();
            Color color = control.hueIcon.getColor();
            Node n = new Node(p, radius, color,"c");
            n.setSelected(true);
            nodes.add(n);
            repaint();
        }
    }

    private class RandomAction extends AbstractAction {

        public RandomAction(String name) {
            super(name);
        }

        public void actionPerformed(ActionEvent e) {
            for (int i = 0; i < 16; i++) {
                Point p = new Point(rnd.nextInt(getWidth()), rnd.nextInt(getHeight()));
                nodes.add(new Node(p, radius, new Color(rnd.nextInt()),"c"));
            }
            repaint();
        }
    }
    
    private class ContinueAction extends AbstractAction {

        public ContinueAction(String name) {
            super(name);
        }

        public void actionPerformed(ActionEvent e) {
            cont=true;
        }
    }
    
    private class bfAction extends AbstractAction {

    	
    	public bfAction(String string) {
    		super(string);
    	}

    	public void actionPerformed(ActionEvent e) {
    		Thread t = new Thread(new BellmanFord());
    		izlaz.setText("Starting Bellman-Ford");
    		t.start();
    		
    		repaint();
    	}

    }
    
    private class BellmanFord implements Runnable{

    	private HashMap<Node,Integer> udaljenosti=new HashMap<Node,Integer>();
    	private HashMap<Node,Node> predhodnik=new HashMap<Node,Node>();
    	private final int INF=10000;
    	private Node start;
    	
		@Override
		public void run() {
			Node.getSelected(nodes, selected);
    		if(selected.size()==1){
    			start=selected.get(0);
    		}
    		else{
    			start=nodes.get(ThreadLocalRandom.current().nextInt(nodes.size()));
    		}
    		izlaz.setText("Odabran pocetni cvor: "+start);
    		
    		for (Node node : nodes) {
				udaljenosti.put(node, INF);
				predhodnik.put(node, null);
			}
    		udaljenosti.replace(start, INF, 0);
    		boolean imaPromjene=true;
    		for(int i=0;i<nodes.size()-1;i++){
    			
    			if(!imaPromjene){
    				break;
    			}
    			imaPromjene=false;
    			for (Edge edge : edges) {
    				izlaz.setText(udaljenosti.toString()+"\n"+predhodnik.toString());
    				cont=!control.koraci.isSelected();
    				edge.color=Color.BLUE;
    				repaint();
					Node u = edge.n1;
					Node v = edge.n2;
					int w = edge.weight;
					if(udaljenosti.get(u)+w<udaljenosti.get(v)){
						udaljenosti.replace(v, udaljenosti.get(u)+w);
						predhodnik.replace(v, u);
						imaPromjene=true;
					}
					else if(!edge.directivity && udaljenosti.get(v)+w<udaljenosti.get(u)){
						udaljenosti.replace(u, udaljenosti.get(v)+w);
						predhodnik.replace(u, v);
						imaPromjene=true;
					}
					while(!cont){
						try {
							Thread.sleep(10);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					edge.color=Color.DARK_GRAY;
				}
    		}
    		for (Edge edge : edges) {
    			Node u = edge.n1;
				Node v = edge.n2;
				int w = edge.weight;
				if(udaljenosti.get(u)+w<udaljenosti.get(v)){
					izlaz.setText("Otkriven ciklus negativne tezine");
				}
			}
    		izlaz.setText(izlaz.getText()+"\nBellman-Ford ended");
		}
    	
    }

    private static class ColorIcon implements Icon {

        private static final int WIDE = 20;
        private static final int HIGH = 20;
        private Color color;

        public ColorIcon(Color color) {
            this.color = color;
        }

        public Color getColor() {
            return color;
        }

        public void setColor(Color color) {
            this.color = color;
        }

        public void paintIcon(Component c, Graphics g, int x, int y) {
            g.setColor(color);
            g.fillRect(x, y, WIDE, HIGH);
        }

        public int getIconWidth() {
            return WIDE;
        }

        public int getIconHeight() {
            return HIGH;
        }
    }
}