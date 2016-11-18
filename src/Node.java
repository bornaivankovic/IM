import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;


/**
 * A Node represents a node in a graph.
 */
public class Node {

    public Point p;
    public int r;
    public Color color;
    public boolean selected = false;
    public Rectangle b = new Rectangle();
    public String name;

    /**
     * Construct a new node.
     */
    public Node(Point p, int r, Color color,String name) {
        this.p = p;
        this.r = r;
        this.color = color;
        this.name=name;
        setBoundary(b);
    }
    
    public Node(String str){
    	this.name=str;
    }

    /**
     * Calculate this node's rectangular boundary.
     */
    private void setBoundary(Rectangle b) {
        b.setBounds(p.x - r, p.y - r, 2 * r, 2 * r);
    }

    /**
     * Draw this node.
     */
    public void draw(Graphics g) {
        g.setColor(this.color);
        g.drawOval(b.x, b.y, b.width, b.height);
        g.setColor(Color.BLACK);
        g.setFont(new Font("Times Roman", Font.PLAIN, 24));
        g.drawString(this.name, b.x+r, b.y+r);
        if (selected) {
            g.setColor(Color.darkGray);
            g.drawOval(b.x, b.y, b.width, b.height);
        }
    }

    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Node other = (Node) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	
	@Override
	public String toString(){
		return this.name;
	}

	/**
     * Return this node's location.
     */
    public Point getLocation() {
        return p;
    }

    /**
     * Return true if this node contains p.
     */
    public boolean contains(Point p) {
        return b.contains(p);
    }

    /**
     * Return true if this node is selected.
     */
    public boolean isSelected() {
        return selected;
    }

    /**
     * Mark this node as selected.
     */
    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    /**
     * Collected all the selected nodes in list.
     */
    public static void getSelected(List<Node> list, List<Node> selected) {
        selected.clear();
        for (Node n : list) {
            if (n.isSelected()) {
                selected.add(n);
            }
        }
    }

    /**
     * Select no nodes.
     */
    public static void selectNone(List<Node> list) {
        for (Node n : list) {
            n.setSelected(false);
        }
    }

    /**
     * Select a single node; return true if not already selected.
     */
    public static boolean selectOne(List<Node> list, Point p) {
        for (Node n : list) {
            if (n.contains(p)) {
                if (!n.isSelected()) {
                    Node.selectNone(list);
                    n.setSelected(true);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Select each node in r.
     */
    public static void selectRect(List<Node> list, Rectangle r) {
        for (Node n : list) {
            n.setSelected(r.contains(n.p));
        }
    }

    /**
     * Toggle selected state of each node containing p.
     */
    public static void selectToggle(List<Node> list, Point p) {
        for (Node n : list) {
            if (n.contains(p)) {
                n.setSelected(!n.isSelected());
            }
        }
    }

    /**
     * Update each node's position by d (delta).
     */
    public static void updatePosition(List<Node> list, Point d) {
        for (Node n : list) {
            if (n.isSelected()) {
                n.p.x += d.x;
                n.p.y += d.y;
                n.setBoundary(n.b);
            }
        }
    }

    /**
     * Update each node's radius r.
     */
    public static void updateRadius(List<Node> list, int r) {
        for (Node n : list) {
            if (n.isSelected()) {
                n.r = r;
                n.setBoundary(n.b);
            }
        }
    }

    /**
     * Update each node's color.
     */
    public static void updateColor(List<Node> list, Color color) {
        for (Node n : list) {
            if (n.isSelected()) {
                n.color = color;
            }
        }
    }

}