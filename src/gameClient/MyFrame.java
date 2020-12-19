package gameClient;

import api.*;
import gameClient.util.Point3D;
import gameClient.util.Range;
import gameClient.util.Range2D;

import javax.swing.*;
import java.awt.*;
import java.util.Iterator;
import java.util.List;

/**
 * This class represents a very simple GUI class to present a
 * game on a graph - you are welcome to use this class - yet keep in mind
 * that the code is not well written in order to force you improve the
 * code and not to take it "as is".
 *
 */
public class MyFrame extends JFrame {
	private Image dbImage;
	private Graphics dbg;
	private int _ind;
	private Arena _ar;
	private gameClient.util.Range2Range _w2f;
	MyFrame(String a) {
		super(a);
		int _ind = 0;
	}
	public void update(Arena ar) {
		this._ar = ar;
		updateFrame();
	}
	private void updateFrame() {
		Range rx = new Range(100,this.getWidth()-250);
		Range ry = new Range(this.getHeight()-50,250);
		Range2D frame = new Range2D(rx,ry);
		directed_weighted_graph g = _ar.getGraph();
		_w2f = Arena.w2f(g,frame);
	}
	public void paint(Graphics g) {
		dbImage = createImage(getWidth(),getHeight());
		dbg = dbImage.getGraphics();
		paintComp(dbg);
		g.drawImage(dbImage,0,0,this);
	}
	public void paintComp(Graphics g){
		int w = this.getWidth();
		int h = this.getHeight();
		g.clearRect(0, 0, w, h);
		updateFrame();
		drawGraph(g);
		drawPokemons(g);
		drawAgants(g);
		drawInfo(g);
		drawDetails(g);

	}
	private void drawInfo(Graphics g) {
		List<String> str = _ar.get_info();
		String dt = "none";
		for(int i=0;i<str.size();i++) {
			g.drawString(str.get(i)+" dt: "+dt,100,60+i*20);
		}
		
	}
	private void drawGraph(Graphics g) {
		directed_weighted_graph gg = _ar.getGraph();
		Iterator<node_data> iter = gg.getV().iterator();
		while(iter.hasNext()) {
			node_data n = iter.next();
			g.setColor(Color.BLUE);
//			drawNode(n,10,g);
			Iterator<edge_data> itr = gg.getE(n.getKey()).iterator();
			while(itr.hasNext()) {
				edge_data e = itr.next();
				g.setColor(Color.BLACK);
				drawEdge(e, g);
			}
			//draw node here for agent to paint over edge
			drawNode(n,12,g);
		}
	}
	private void drawPokemons(Graphics g) {
		List<CL_Pokemon> fs = _ar.getPokemons();
		if(fs!=null) {
		Iterator<CL_Pokemon> itr = fs.listIterator();
		
		while(itr.hasNext()) {
			
			CL_Pokemon f = itr.next();
			Point3D c = f.getLocation();
			int r=18;
			Graphics2D g2 = (Graphics2D)g;
			Toolkit tk = Toolkit.getDefaultToolkit();
			Image type1 = tk.getImage("pokemon_Type1.png");
			Image typeMinus1 = tk.getImage("pokemon_TypeMinus1.png");
			Image img = type1;

			if(f.getType()<0) {img = typeMinus1;}
			if(c!=null) {
				geo_location fp = this._w2f.world2frame(c);
				g2.drawImage(img,(int)fp.x()-r,(int)fp.y()-r,2*r,2*r,this);

			}
		}
		}
	}
	private void drawAgants(Graphics g) {
		List<CL_Agent> rs = _ar.getAgents();
		int i=0;
		Toolkit tk = Toolkit.getDefaultToolkit();
		Image img = tk.getImage("agent.png");
		while(rs!=null && i<rs.size()) {
			geo_location c = rs.get(i).getLocation();
			int r=26;
			i++;
			if(c!=null) {
				geo_location fp = this._w2f.world2frame(c);
				Graphics2D g2 = (Graphics2D) g;
				g2.drawImage(img,(int)fp.x()-r,(int)fp.y()-r,2*r,2*r,this);
			}
		}
	}
	private void drawNode(node_data n, int r, Graphics g) {
		geo_location pos = n.getLocation();
		geo_location fp = this._w2f.world2frame(pos);
		Graphics2D g2 = (Graphics2D)g;
		Toolkit tk = Toolkit.getDefaultToolkit();
		Image stopNode = tk.getImage("nodePokemonGO.png");
		g2.drawImage(stopNode,(int)fp.x()-2*r,(int)fp.y()-2*r-20,4*r,4*r,this);
		g.setFont(new Font("name",Font.BOLD,15));
		g.setColor(Color.BLUE);
		g.drawString(""+n.getKey(), (int)fp.x()-8, (int)fp.y()+4*r-58);
	}
	private void drawEdge(edge_data e, Graphics g) {
		directed_weighted_graph gg = _ar.getGraph();
		geo_location s = gg.getNode(e.getSrc()).getLocation();
		geo_location d = gg.getNode(e.getDest()).getLocation();
		geo_location s0 = this._w2f.world2frame(s);
		geo_location d0 = this._w2f.world2frame(d);
		Graphics2D g2 = (Graphics2D) g;
		g2.setStroke(new BasicStroke(3));
		g2.drawLine((int)s0.x(), (int)s0.y(), (int)d0.x(), (int)d0.y());

	}
	private void drawDetails(Graphics g){
		geo_location geo = _w2f.getFrame().fromPortion(new Point3D(0,0,0));
		int i = (int)(Math.random()*1000);
//		System.out.println(i);
		int x = (int)geo.x()+i;
		int y = (int)geo.y()+i;
		g.setColor(Color.BLACK);
		g.setFont(new Font("name",Font.BOLD,20));
		Ex2 client = new Ex2();

		String s = "ID: "+client.idToJSON() +
		" \nLevel: "+client.gameLevelToJSON()+"Time Left: "+client.timeToEnd();
		g.drawString(new String("Logged: "+client.isLoggedToJSON()),1100,70);
		g.drawString(new String("ID: "+client.idToJSON()),1100,100);
		g.drawString(new String("Moves: "+client.movesToJSON()),1100,130);
		g.drawString(new String("Level: "+client.gameLevelToJSON()),1100,160);
		g.drawString(new String("Max Pokemons: "+client.pokeToJSON()),1100,190);
		g.drawString(new String("Agents: "+client.anAgentInt()),1100,220);
		g.drawString(new String("Grade: "+client.gradeToJSON()),1100,250);
		g.drawString(new String("Time Left: "+((client.timeToEnd()/1000))),1100,280);
	}


}
