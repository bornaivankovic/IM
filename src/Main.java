import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import javax.swing.JFrame;
import javax.swing.JScrollPane;

public class Main {

	public static void main(String[] args) throws IOException {
		ArrayList<Node> nodes = new ArrayList<>();
		ArrayList<Edge> edges = new ArrayList<>();
		parseFile("mreza.txt", nodes, edges);
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				createAndShowGUI(nodes, edges);
			}
		});
	}

	private static void createAndShowGUI(ArrayList<Node> nodes, ArrayList<Edge> edges) {
		JFrame frame = new JFrame("Bellman-Ford");
		frame.setPreferredSize(new Dimension(850, 700));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		GraphPanel gp = new GraphPanel();
		gp.setPreferredSize(new Dimension(800, 600));
		frame.getContentPane().add(gp.control, BorderLayout.NORTH);
		JScrollPane scrollPane = new JScrollPane(gp);
		scrollPane.setPreferredSize(new Dimension(800, 600));
		frame.getContentPane().add(scrollPane, BorderLayout.CENTER);

		gp.nodes = nodes;
		gp.edges = edges;

		frame.pack();
		frame.setVisible(true);
	}

	private static void parseFile(String filename, ArrayList<Node> nodes, ArrayList<Edge> edges) throws IOException {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(filename));
			String line;
			while ((line = br.readLine()) != null) {
				String[] redak = line.split(" ");
				Node n1 = new Node(redak[0]);
				Node n2 = new Node(redak[1]);
				if (!nodes.contains(n1)) {
					nodes.add(n1);
				} else {
					n1 = nodes.get(nodes.indexOf(n1));
				}
				if (!nodes.contains(n2)) {
					nodes.add(n2);
				} else {
					n2 = nodes.get(nodes.indexOf(n2));
				}
				Edge e = new Edge(n1, n2, redak[3].equals("u"), Integer.parseInt(redak[2]));
				edges.add(e);
			}
			Set<Point> tocke = new HashSet<>();
			do {
				Point a = new Point(ThreadLocalRandom.current().nextInt(50, 400),
						ThreadLocalRandom.current().nextInt(50, 400));
				tocke.add(a);
			} while (tocke.size() < nodes.size());
			Object[] t = new ArrayList<>(tocke).toArray();
			int i = 0;
			for (Node node : nodes) {
				node.p = (Point) t[i];
				node.r = GraphPanel.RADIUS;
				node.color = Color.RED;
				node.b.setBounds(node.p.x - node.r, node.p.y - node.r, 2 * node.r, 2 * node.r);
				i++;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			br.close();
		}
	}
}
