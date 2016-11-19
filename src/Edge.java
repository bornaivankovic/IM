import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;

/**
 * An Edge is a pair of Nodes.
 */
public class Edge {

    public Node n1;
    public Node n2;
    public boolean directivity;
    public int weight;
    public Color color=Color.DARK_GRAY;

    public Edge(Node n1, Node n2, boolean directivity, int weight) {
        this.n1 = n1;
        this.n2 = n2;
        this.directivity=directivity;
        this.weight=weight;
    }

    public Edge() {
	}

	public void draw(Graphics g) {
        Point p1 = n1.getLocation();
        Point p2 = n2.getLocation();
        g.setColor(color);
        
        if(!directivity){
        	g.drawLine(p1.x, p1.y, p2.x, p2.y);
        	g.drawString(String.valueOf(weight), (p1.x+p2.x)/2, (p1.y+p2.y)/2);
        }
        else{
        	
        	int offset = 15;
        	if(p1.x<p2.x)
        		offset=-offset;
			g.drawLine(p1.x+offset, p1.y+offset, p2.x+offset, p2.y+offset);
			g.drawString(String.valueOf(weight)+"("+n1.name+","+n2.name+")", (p1.x+p2.x)/2+offset, (p1.y+p2.y)/2+offset);
        }
        
    }
    
    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (directivity ? 1231 : 1237);
		result = prime * result + ((n1 == null) ? 0 : n1.hashCode());
		result = prime * result + ((n2 == null) ? 0 : n2.hashCode());
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
		Edge other = (Edge) obj;
		if (directivity != other.directivity)
			return false;
		if(directivity==true){
			if (n1 == null) {
				if (other.n1 != null)
					return false;
			} else if (!n1.equals(other.n1))
				return false;
			if (n2 == null) {
				if (other.n2 != null)
					return false;
			} else if (!n2.equals(other.n2))
				return false;
			return true;
		}
		else{
			if (n1 == null) {
				if (other.n1 != null)
					return false;
			} else if (!n1.equals(other.n1) || !n1.equals(other.n2))
				return false;
			if (n2 == null) {
				if (other.n2 != null)
					return false;
			} else if (!n2.equals(other.n2) || !n2.equals(other.n1))
				return false;
			return true;
		}
	}

	@Override
    public String toString(){
    	String separator;
    	if(this.directivity)
    		separator="->";
    	else
    		separator="-";
		return n1.toString()+separator+n2.toString();
    }
}
