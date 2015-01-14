/*
 * Fire Emblem
 * Kevin Xue and Lloyd Torres
 *
 * Fire Emblem is a two-player turn-based strategy game. Players select their initial
 * units, pick a map, and fight against the other player. They can purchase new
 * units during the game. The end goal is to either destroy all of the other player's
 * units.
 *
 * This program uses JFrames and JPanels for graphics (Java Swing) and makes use
 * of the files found within the Fire Emblem folder.
 *
 * This program has the following classes:
 * - FireEmblem: main class, keeps track of other classes, handles switching between
 *	 JPanels.
 * - MainMenu: JPanel that shows the main menu
 * - PlayerSelect: JPanel that shows the unit selection screen, keeps track of cursor
 *                 and draws images
 * - SelectWindow: companion class to PlayerSelect; handles sprite display,
 *	 			   keeps track of selections
 * - PickTerrain: JPanel that shows the map selection screen
 * - Terrain: opens selected map, draws map on screen, keeps track of terrain defence
 *            values and actual map layout
 * - Unit: used to create Unit objects, which holds stats and images for each unit
 *         on the game
 * - Drawer: master JPanel for the game; draws units, maps, UI stuff
 * - Winner: JPanel shown at the end which displays which player won the game
 */
 
 ///// MODULES
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.imageio.*;
import javax.swing.*; 
import java.io.*; 
import java.util.*;
import java.lang.*;

///// FireEmblem Main Class
///// Keeps track of all other classes used within the game.
public class FireEmblem extends JFrame implements ActionListener{
	//// Variables
	private MainMenu menu; // classes used for game
	private PlayerSelect ps;
	private SelectWindow sw;
	private PickTerrain pt;
	private Drawer drawer;
	private Terrain mapTrack;
	private Winner win;
	
	private javax.swing.Timer myTimer; // timer to "slow down" game speed
	
	private int screen = 0; // keeps track of which JPanel to use
	
	private ArrayList<String> p1; // holds unit selections for player 1 from PlayerSelect
	private ArrayList<String> p2; // ditto, but for player 2
	private int[][] positions=new int[15][20]; // 
	private Image[][] overworld = new Image[11][3];	// 2D Array of generic overworld sprites
	private Image[] portrait1=new Image[10]; // holds unit faces for player 1
	private Image[] portrait2=new Image[10]; // ditto, but for player 2
	private String[] roster={"Archer","Berserker","Cavalier","Cleric","Wyvern","General","Halberdier","Pegasus","Sage","Swordsman"}; // array of all Unit names
	private int[][] unitPos=new int[15][20]; // stores units as integers; used in initial unit set-up and placement
	private Unit[][] units=new Unit[15][20]; // stores actual Unit objects in a 2D grid - position determines location on map
	private int moneyleft1,moneyleft2; // holds money left over from unit select screen; passed on to main game
	
	//// Constructor
	public FireEmblem(){
		super("Fire Emblem"); // sets title
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // ensures the window closes properly
		setLayout(null); // not using any specific layout
		setSize(802,669); // sets windows size
		
		for (int i=1;i<4;i++){ // loading generic sprite images
			overworld[1][i-1]=(new ImageIcon("Sprites/Select/archer"+i+".png").getImage());	//0
			overworld[2][i-1]=(new ImageIcon("Sprites/Select/berserker"+i+".png").getImage());
			overworld[3][i-1]=(new ImageIcon("Sprites/Select/cavalier"+i+".png").getImage());
			overworld[4][i-1]=(new ImageIcon("Sprites/Select/cleric"+i+".png").getImage());
			overworld[5][i-1]=(new ImageIcon("Sprites/Select/wyvern"+i+".png").getImage());
			overworld[6][i-1]=(new ImageIcon("Sprites/Select/general"+i+".png").getImage());
			overworld[7][i-1]=(new ImageIcon("Sprites/Select/halberdier"+i+".png").getImage());
			overworld[8][i-1]=(new ImageIcon("Sprites/Select/pegasus"+i+".png").getImage());
			overworld[9][i-1]=(new ImageIcon("Sprites/Select/sage"+i+".png").getImage());
			overworld[10][i-1]=(new ImageIcon("Sprites/Select/swordsman"+i+".png").getImage());//9
		}

		portrait1[0]=(new ImageIcon("Sprites/Portrait/archer1.png").getImage()); // loading portraits for each player 1 unit	
		portrait1[1]=(new ImageIcon("Sprites/Portrait/berserker1.png").getImage());
		portrait1[2]=(new ImageIcon("Sprites/Portrait/cavalier1.png").getImage());
		portrait1[3]=(new ImageIcon("Sprites/Portrait/cleric1.png").getImage());
		portrait1[4]=(new ImageIcon("Sprites/Portrait/wyvern1.png").getImage());
		portrait1[5]=(new ImageIcon("Sprites/Portrait/general1.png").getImage());
		portrait1[6]=(new ImageIcon("Sprites/Portrait/halberdier1.png").getImage());
		portrait1[7]=(new ImageIcon("Sprites/Portrait/pegasus1.png").getImage());	
		portrait1[8]=(new ImageIcon("Sprites/Portrait/sage1.png").getImage());	
		portrait1[9]=(new ImageIcon("Sprites/Portrait/swordsman1.png").getImage());
		
		portrait2[0]=(new ImageIcon("Sprites/Portrait/archer2.png").getImage()); // loading portraits for each player 2 unit		
		portrait2[1]=(new ImageIcon("Sprites/Portrait/berserker2.png").getImage());
		portrait2[2]=(new ImageIcon("Sprites/Portrait/cavalier2.png").getImage());
		portrait2[3]=(new ImageIcon("Sprites/Portrait/cleric2.png").getImage());
		portrait2[4]=(new ImageIcon("Sprites/Portrait/wyvern2.png").getImage());
		portrait2[5]=(new ImageIcon("Sprites/Portrait/general2.png").getImage());
		portrait2[6]=(new ImageIcon("Sprites/Portrait/halberdier2.png").getImage());
		portrait2[7]=(new ImageIcon("Sprites/Portrait/pegasus2.png").getImage());	
		portrait2[8]=(new ImageIcon("Sprites/Portrait/sage2.png").getImage());	
		portrait2[9]=(new ImageIcon("Sprites/Portrait/swordsman2.png").getImage());
		
		menu = new MainMenu(); // creates first JPanel - main menu
		add(menu); // adds JPanel to JFrame
		
		myTimer = new javax.swing.Timer(100,this); // timer at 100 millisecond, used to slow down game
		myTimer.start();
		
		setResizable(false); // prevents users from resizing window
		setVisible(true); // makes window visible
	}
	
	//// Methods
	public void actionPerformed(ActionEvent evt){ // actionPerform class inherited from JFrame, listens for events
		Object source = evt.getSource(); // gets sources of events from Windows
		if(source == myTimer){ // once 100 ms passes
			if (screen == 0){ // screen 0 - main menu
				menu.moveCursor(); // update cursor
				menu.repaint(); // redraw contents
				if (menu.picked()){ // if user picks
					screen = 1; // move on to screen 1 - player select
					remove(menu); // remove MainMenu JPanel
					sw=new SelectWindow(overworld,portrait1,portrait2,roster); // create SelectWindow companion class and PlayerSelect JPanel
					ps=new PlayerSelect(sw);
					add(ps); // add new JPanel
				}
			}
			else if (screen == 1){ // screen 1 - PlayerSelect
				ps.moveCursor(); // update cursor
				ps.repaint(); // redraw contents
				if (!ps.isPicking()){ // if player is done picking
					p1 = sw.getPlayer1(); // pass on p1 selections to main class
					p2 = sw.getPlayer2(); // ditto for player 2
					remove(ps); // remove current JPanel, create PickTerrain JPanel, add that JPanel, switch screens
					pt = new PickTerrain();
					add(pt);
					screen = 2;
				}
			}
			else if (screen == 2){ // screen 2 - PickTerrain
				pt.moveCursor(); // update cursor
				pt.repaint(); // redraw contents
				if (pt.picked() != -1){ // once a map has been picked
					mapTrack = new Terrain(pt.picked()); // create new Terrain object
					remove(pt); // remove pickterrain JPanel
					
					drawer = new Drawer(mapTrack,p1,p2,portrait1,portrait2,roster); // set up Drawer; pass in Terrain, unit selections, portrait pictures, unit names
					drawer.placeUnits(); // temporarily puts units into grid for unit placement
					add(drawer); // add drawer JPanel
					screen = 3; // switch to screen 3
				}
			}
			else if (screen == 3){ // screen 3 - actual game
				drawer.moveCursor(); // update cursor
				drawer.repaint(); // redraw contents
				if(drawer.getMode()==2 && !drawer.isPicking()){ // if drawer is in mode 2 (unit placement) and players are done placing units
					drawer.setMode(1); // set mode 0 -> player 1's turn
					unitPos=drawer.getUnitPos(); // get final unit placements
					units=convert(); // turns int grid into Unit object grid
					drawer.setUnit(units); // give converted units back to drawer
				}
				else if(drawer.getMode()==1){ // during gameplay
					if (drawer.checkWin() == 0){ // see if someone has won
						drawer.switchTurn(); // if not, switch to other player
					}
					else{ // if winner
						remove(drawer); // switch JPanel to Winner JPanel
						win = new Winner(drawer.checkWin());
						add(win);
						screen = 4;
					}
				}
			}
			else if (screen == 4){ // screen 4 - winner JPanel
				win.move(); // checks for user action
				win.repaint(); // draws background
				if(win.done()){ // if user is done, exit program
					System.exit(0);
				}
			}
		}
	}
	
	public Unit[][] convert(){ // converts 2D Array of int to 2D Array of actual Unit objects
		for (int r=0;r<15;r++){ // goes through each row and column of unitPos
			for(int c=0;c<20;c++){
				if (unitPos[r][c]!=0){ // if not empty
					if (c<8){ // if from upper half of screen
						units[r][c]=new Unit(roster[unitPos[r][c]-1],mapTrack,1); // create Unit based on number on 2D Array (e.g. 1 -> archer) for player 1
					}
					else{
						units[r][c]=new Unit(roster[unitPos[r][c]-1],mapTrack,2); // ditto for player 2
					}
				}				
			}
		}
		return units; // give back converted 2D Array
	}
	
	public static void main(String[]args){ // main method
		FireEmblem window = new FireEmblem();
	}
}

///// MainMenu Menu JPanel Class
///// JPanel that shows main menu for the game; uses KeyListener to check for keyboard input to move the cursor.
class MainMenu extends JPanel implements KeyListener{ 
	private boolean[]keys; // boolean Array used to keep track of which buttons are being pressed
	private Image back = new ImageIcon("Sprites/MenuBack.jpg").getImage(); // background
	private int cursor = 0; // cursor position on screen
	private boolean userPicked = false; // flag checking if user has made a choice
	
	public MainMenu(){
		super();
		keys = new boolean[KeyEvent.KEY_LAST+1];
		setSize(802,669);
		addKeyListener(this);
	}
	
	public void addNotify(){ // method inherited from JPanel
		super.addNotify();
		requestFocus(); // makes sure that Windows puts focus into this window
	}
	
	public void moveCursor(){ // method checks for keyboard input and does things accordingly
		if (keys[KeyEvent.VK_W] && cursor == 1){ // up (W key)
			cursor = 0;
		}
		if (keys[KeyEvent.VK_S] && cursor == 0){ // down (S key)
			cursor = 1;
		}
		if (keys[KeyEvent.VK_J]){ // confirm (J key)
			if (cursor == 0){ // play
				userPicked = true;
			}
			else if (cursor == 1){ // exit
				System.exit(0);
			}
		}
	}
	
	// keyboard methods from JPanel
	public void keyTyped(KeyEvent e){}
	
	public void keyPressed(KeyEvent e){ // sets keys Array to determine if key is being pressed or not
		keys[e.getKeyCode()] = true;
	}
	public void keyReleased(KeyEvent e){
		keys[e.getKeyCode()] = false;
	}
	
	public boolean picked(){ // notifies other classes if user has finished picking or not
		return userPicked;
	}
	
	private void drawCursor(Graphics g){ // draws cursor on menu
		g.setColor(new Color(255,255,255)); // colour -> white
		g.fillRect(72,359+109*cursor,32,32); // choices are spaced 109 px apart in y axis
	}
	
	public void paintComponent(Graphics g){ // method adopted from JPanel, draws graphics into screen
		g.drawImage(back,0,0,this);
		drawCursor(g);
	}
	
}

///// PlayerSelect JPanel Class
///// JPanel for unit selection for each player; does most of the drawing.
///// Uses SelectWindow class, which does most of the heavy lifting.
class PlayerSelect extends JPanel implements KeyListener{
	private boolean[]keys; // boolean Array to determine which keys are pressed
	private Image bcursor=new ImageIcon("Sprites/BlueSelector.png").getImage(); // get image of blue p1 cursor
	private int bcx,bcy; // position of p1's cursor
	private Image rcursor=new ImageIcon("Sprites/RedSelector.png").getImage(); // get image of red p2 cursor
	private int rcx,rcy; // position of p2's cursor
	private Image gcursor=new ImageIcon("Sprites/GreenSelector.png").getImage(); // get image of green p1 and p2 cursor
	private Image okCursor=new ImageIcon("Sprites/ok_highlight.png").getImage(); // highlighed OK button
	private Image okReady=new ImageIcon("Sprites/ok_ready.png").getImage(); // ready button
	private boolean p1ready = false; // if p1 finished picking
	private boolean p2ready = false; // if p2 finished picking
	SelectWindow sw; // used to call SelectWindow class
	
	//// constructor
	public PlayerSelect(SelectWindow nsw){
		super();
		keys = new boolean[KeyEvent.KEY_LAST+1]; // starts up keyboard tracking
		setSize(802,669); // set panel size
		bcx = 1; // starting cursor positions
		bcy = 0;
		rcx = 3;
		rcy = 0;
		
		sw=nsw;
		
		addKeyListener(this);
	}
	
	public void addNotify(){ // method inherited from JPanel
		super.addNotify();
		requestFocus(); // makes sure that Windows puts focus into this window
	}

	public void moveCursor(){ // method checks for keyboard input and does things accordingly
		if (!p1ready){ // player 1
			if (keys[KeyEvent.VK_D] && bcx < 4){ // right
				bcx += 1;
			}
			if (keys[KeyEvent.VK_A] && bcx > -1){ // left
				bcx -= 1;
			}
			if (keys[KeyEvent.VK_W] && bcy > 0){ // up
				bcy -= 1;
			}
			if (keys[KeyEvent.VK_S] && bcy < 1){ // down
				bcy += 1;
			}
			if (bcx != -1){
				if (keys[KeyEvent.VK_J]){ // confirm; add unit to selections
					sw.add(1,bcx,bcy);			
				}
				if (keys[KeyEvent.VK_K]){ // remove unit from selections
					sw.remove(1);
				}
			}
			else{
				if (keys[KeyEvent.VK_J] && sw.p1length() != 0){ // if cursor is over "OK" button and player has some units selected
					p1ready = true;			
				}
			}
		}
		if (keys[KeyEvent.VK_K] && p1ready){ // if p1 is already ready but presses undo
			p1ready = false;
		}
		
		if (!p2ready){ // player 2
			if (keys[KeyEvent.VK_RIGHT] && rcx < 5){ // right
				rcx += 1;
			}
			if (keys[KeyEvent.VK_LEFT] && rcx > 0){ // left
				rcx -= 1;
			}
			if (keys[KeyEvent.VK_UP] && rcy > 0){ // up
				rcy -= 1;
			}
			if (keys[KeyEvent.VK_DOWN] && rcy < 1){ // down
				rcy += 1;
			}
			if (rcx != 5){
				if (keys[KeyEvent.VK_NUMPAD1]){ // add to selection
					sw.add(2,rcx,rcy);
				}
				if (keys[KeyEvent.VK_NUMPAD2]){ // remove from selection
					sw.remove(2);
				}
			}
			else{
				if (keys[KeyEvent.VK_NUMPAD1] && sw.p2length() != 0){ // if cursor is over OK button and units have been selected
					p2ready = true;
				}
			}
		}
		if (keys[KeyEvent.VK_NUMPAD2] && p2ready){ // if already ready but p2 cancels
			p2ready = false;
		}
	sw.feedCursor1(bcx,bcy); // gives cursor positions to SelectWindow
	sw.feedCursor2(rcx,rcy);
	}

	// keyboard methods from JPanel
	public void keyTyped(KeyEvent e){}
	
	public void keyPressed(KeyEvent e){ // sets keys true if pressed, false if released
		keys[e.getKeyCode()] = true;
	}
	public void keyReleased(KeyEvent e){
		keys[e.getKeyCode()] = false;
	}
	
	public boolean isPicking(){ // gives status of JPanel to other methods; if both are true, it returns a false (i.e. no longer picking)
		return !(p1ready && p2ready);
	}
	
	private void drawCursors(Graphics g){ // draws cursors on selection grid
		if (bcx == rcx && bcy == rcy){ // if both p1 and p2 cursors are on the same box
			g.drawImage(gcursor,300+bcx*40,300+bcy*40,null); // draw green cursor
		}
		else{
			if (p1ready){ // if p1 is ready, draw ready button
				g.drawImage(okReady,102,344,null);
			}
			else if (bcx == -1){ // if over OK button
				g.drawImage(okCursor,102,344,null);
			}
			else{ // draw normally otherwise
				g.drawImage(bcursor,300+bcx*40,300+bcy*40,null);
			}
			
			if (p2ready){ // ditto above, but for p2 - ready button
				g.drawImage(okReady,560,344,null);
			}
			else if (rcx == 5){ // over ok button
				g.drawImage(okCursor,560,344,null);
			}
			else{ // normal draw
				g.drawImage(rcursor,300+rcx*40,300+rcy*40,null);	
			}	
		}
	}
	
	public void paintComponent(Graphics g){ // method adopted from JPanel, draws graphics into screen
		sw.draw(g);
		drawCursors(g);
	}
}

///// SelectWindow Class (Companion to PlayerSelect)
///// Does the heavy lifting for unit select; takes on some drawing tasks.
class SelectWindow{
	private Image[][] overworld = new Image[11][3];	//2D Array of overworld sprites
	private Image[] portrait1=new Image[10]; // portraits of player 1 units
	private Image[] portrait2=new Image[10]; // ditto but for player 2
	private Image world; // used for background
	private int bcx = 1; // initial cursor positions; b -> p1, r -> p2
	private int bcy = 0;
	private int rcx = 3;
	private int rcy = 0;
	
	private int beat = 1; // used for animations - goes from 0 to 2; number controls which version of image to be shown on screen
	private int beatCount = 0; // counter modded to 2; if divisible, program will continue
	private int beatDir = 1; // determines whether or not beat will be going up or down
	
	private File ttf, ttf2; // holds TTF files to load later
	private Font font, font2; // turns TTF files to usable fonts in Java
	
	private String[] roster; // names of each unit; grabs it from FireEmblem
	private int[] costs={200,600,500,100,1000,800,100,1000,300,150}; // price of each unit, same order as roster
	private String[] desc={"[Long Range]",				// descriptions for each unit, same order as roster
							"+HP, +Strength",
							"+Movement",
							"[Heals]",
							"[Fly] +HP, +Defence",
							"+Strength, + Defence",
							"+Speed, +Skill",
							"[Fly] +Speed",
							"+Attack Range",
							"+Critical Hits"};
	private int money1=2000; // funds available to p1
	private int money2=2000; // funds available to p2
	private ArrayList<String> p1units=new ArrayList(); // unit selections for p1
	private ArrayList<String> p2units=new ArrayList(); // ditto but for p2
	
	// consturctor
	// takes in overworld sprites, p1 and p2 unit portraits, and unit names from FireEmblem
	public SelectWindow(Image[][] noverworld,Image[] nportrait1,Image[] nportrait2,String[] nroster){ 
		overworld=noverworld; // puts in taken variables into its own variables
		portrait1=nportrait1;
		portrait2=nportrait2;
		roster=nroster;
		
		world=(new ImageIcon("Sprites/Background.jpg").getImage());	// gets background image

		ttf = new File("FireEmblemText.ttf"); // grabs TTF file from folder
		ttf2 = new File("Minecraftia.ttf"); // grabs TTF file from folder

		try{
			font = Font.createFont(Font.TRUETYPE_FONT,ttf).deriveFont(Font.PLAIN,40); // turns it to usable font, size 40
			font2 = Font.createFont(Font.TRUETYPE_FONT,ttf2).deriveFont(Font.PLAIN,10); // turns it to usable font, size 10
		}
		catch(IOException ex){
			System.out.println("Achtung! Font nicht gefunden!"); // "Caution! Font not found!"
		}
		catch(FontFormatException ex){
			System.out.println("Achtung! Font nicht gefunden!");
		}
	}
	
	public ArrayList<String> getPlayer1(){ // returns ArrayList of p1 selections
		return p1units;
	}
	
	public ArrayList<String> getPlayer2(){ // ditto but for p2
		return p2units;
	}
	
	private void metronome(){ // used for animations
		if (beatCount > 1000){ // resets to make sure it does not go too big and crash
			beatCount = 0;
		}
		else{
			beatCount += 1; // counter goes up
		}
		
		if (beatCount % 2 == 0){ // if counter divisible to 0, continue
			if (beat == 2){ // if reached upper end, start going down
				beatDir = -1;
			}
			else if (beat == 0){ // if reached lower end, start going up
				beatDir = 1;
			}
			beat += 1 * beatDir;
		}
	}
	
	public void feedCursor1(int x,int y){ // grabs current cursor positions from PlayerSelect
		bcx = x;
		bcy = y;
	}
	
	public void feedCursor2(int x,int y){
		rcx = x;
		rcy = y;
	}
	
	public int p1length(){ // returns size of p1 and p2 selections respectively
		return p1units.size();
	}
	
	public int p2length(){
		return p2units.size();
	}
	
	private void drawGenerics(Graphics g){ // draws generic overworld sprites on a 2 x 5 grid
		for (int r=0;r<2;r++){
			for (int c=0;c<5;c++){							
				g.drawImage(overworld[r*5+c+1][beat],300+c*40,300+r*40,null);								
			}
		}
	}
	
	private void drawUnits1(Graphics g){ // draws p1's selections as portraits
		int index1=0; // used to keep track of positions on 1D ArrayList
		for (int r=0;r<3;r++){ // draws 3 x 5 grid
			for (int c=0;c<5;c++){
				if (index1<p1units.size()){ // to prevent program from going out of bounds
					for (int i=0;i<roster.length;i++){ // goes through each name
						if (roster[i].equals(p1units.get(index1))){ // if position i on roster matches name of unit, draw
							g.drawImage(portrait1[i],40+c*40,200+r*40,null);
						}
					}
					index1+=1; // move on to next item
				}
			}
		}
	}
	
	private void drawUnits2(Graphics g){ // draws p2's selections as portraits
		int index2=0; // used to keep track of positions on 1D ArrayList
		for (int r=0;r<3;r++){ // draws 3 x 5 grid
			for (int c=0;c<5;c++){
				if (index2<p2units.size()){ // to prevent program from going out of bounds
					for (int i=0;i<roster.length;i++){ // goes through each name
						if (roster[i].equals(p2units.get(index2))){ // if position i on roster matches name of unit, draw
							g.drawImage(portrait2[i],559+c*40,200+r*40,null);
						}
					}
					index2+=1; // move on to next item
				}
			}
		}
	}
	
	private void drawInfo1(Graphics2D comp2D,Graphics g){ // draws infobox for p1
		comp2D.setColor(new Color(255,255,255)); // colour -> white
		comp2D.setFont(font); // font -> FireEmblem
		if (bcx > -1){ // if cursor is over a unit; bcx+bcy*5 -> used to get position from 2D Array to 1D Array
			g.drawImage(portrait1[bcx+bcy*5],145,451,null); // draws portrait
			comp2D.drawString(roster[bcx+bcy*5],206,485); // draws name
			comp2D.drawString(""+costs[bcx+bcy*5],206,520); // draws cost
			comp2D.setFont(font2); // set font to Minecraftia
			comp2D.drawString(desc[bcx+bcy*5],206,545); // draws description
		}
	}
	
	private void drawInfo2(Graphics2D comp2D,Graphics g){ // draws infobox for p2
		comp2D.setColor(new Color(255,255,255)); // colour -> white
		comp2D.setFont(font); // font -> FireEmblem
		if (rcx < 5){ // if cursor is over a unit; bcx+bcy*5 -> used to get position from 2D Array to 1D Array
			g.drawImage(portrait2[rcx+rcy*5],437,451,null); // draws portrait
			comp2D.drawString(roster[rcx+rcy*5],498,485); // draws name
			comp2D.drawString(""+costs[rcx+rcy*5],498,520); // draws cost
			comp2D.setFont(font2); // sent font to Minecraftia
			comp2D.drawString(desc[rcx+rcy*5],498,545); // draws description
		}
	}
	
	private void drawMoney (Graphics2D comp2D){ // draws money for p1 and p2
		comp2D.setColor(new Color(255,255,255)); // colour -> white
		comp2D.setFont(font); // set font to FireEmblem
		String moneyString1 = "" + money1; // converts score to a String
		String moneyString2 = "" + money2; // converts score to a String
		comp2D.drawString(moneyString1,80,100); // draws money
		comp2D.drawString(moneyString2,620,100); 
	}
	
	public void draw(Graphics g){ // main draw method
		Graphics2D comp2D = (Graphics2D)g; // taken from example in Mr. McKenzie's Java book (p. 355)
		metronome(); // for animation purposes; draw() is always called so metronome is always called
		g.drawImage(world,0,0,null); // displays background
		drawGenerics(g); // draws overworld sprites
		drawUnits1(g); // draws p1 and p2 portraits
		drawUnits2(g);
		drawInfo1(comp2D,g); // draws infoboxes
		drawInfo2(comp2D,g);
		drawMoney(comp2D); // draws money
	}
	public void add(int i,int cx,int cy){ // used to add units to either p1 or p2
		if (i==1){ // p1
			if (p1units.size()<15&&money1-costs[cx+cy*5]>=0){ // if there's space to add and unit can be afforded
				p1units.add(roster[cx+cy*5]); // add; cx + cy *5 <- conversion from 2D array notation to 1D array
				money1-=costs[cx+cy*5]; // subtracts from p1 money
			}			
		}
		else{ // p2
			if(p2units.size()<15&&money2-costs[cx+cy*5]>=0){ // ditto as above, but for p2
				p2units.add(roster[cx+cy*5]);
				money2-=costs[cx+cy*5];
			}			
		}
	}
	public void remove(int i){ // used to remove units from either p1 or p2
		if (i==1){ // p1
			if (p1units.size()!=0){ // if arraylist is not empty
				for (int j=0;j<roster.length;j++){ // goes through each unit name
					if(roster[j].equals(p1units.get(p1units.size()-1))){ // if unit name matches last entry added
						money1+=costs[j]; // recoup losses
						p1units.remove(p1units.size()-1); // remove unit
						break; // stop looping
					}
				}												
			}			
		}
		else{ // p2
			if (p2units.size()!=0){ // ditto as above, but for p2
				for (int j=0;j<roster.length;j++){
					if(roster[j].equals(p2units.get(p2units.size()-1))){
						money2+=costs[j];
						p2units.remove(p2units.size()-1);
						break;
					}
				}												
			}		
		}
	}
	
}

///// PickTerrain JPanel Class
///// JPanel which displays the terrain selection screen
class PickTerrain extends JPanel implements KeyListener{
	private boolean[]keys; // boolean Array which keeps track of which keys are being pressed
	private Image back = new ImageIcon("Sprites/MapBackground.jpg").getImage(); // background
	private int cursor = 0; // cursor position
	private int userPicked = -1; // keeps track of which map the player has selected, which is passed on to FireEmblem
	private Image[] box = new Image[3]; // holds Images for cursor effect shown on map
	private Integer[] boxX = {173,259,653}; // x and y coordinates for cursor effect
	private Integer[] boxY = {146,498,354};
	
	private int beat = 1; // used for animations - goes from 0 to 2; number controls which version of image to be shown on screen
	private int beatCount = 0; // counter modded to 2; if divisible, program will continue
	private int beatDir = 1; // determines whether or not beat will be going up or down

	public PickTerrain(){
		super();
		keys = new boolean[KeyEvent.KEY_LAST+1];
		setSize(802,669);
		addKeyListener(this);
		
		for (int i=0;i<3;i++){
			box[i] = new ImageIcon("Sprites/SuperSelector"+(i+1)+".png").getImage();
		}
	}
	
	public void addNotify(){ // method inherited from JPanel
		super.addNotify();
		requestFocus(); // makes sure that Windows puts focus into this window
	}
	
	private void metronome(){
		if (beatCount > 1000){ // prevents beatCount from getting too big
			beatCount = 0;
		}
		else{
			beatCount += 1;
		}
		
		if (beatCount % 2 == 0){ // every time beatCount is divisible by 2, continue
			if (beat == 2){ // if upper end of beat is reached, switch direction
				beatDir = -1;
			}
			else if (beat == 0){ // if lower end is reached, switch direction
				beatDir = 1;
			}
			beat += 1 * beatDir;
		}
	}
	
	public void moveCursor(){ // method checks for keyboard input and does things accordingly
		if (keys[KeyEvent.VK_W] && cursor > 0){ // iup
			cursor -= 1;
		}
		if (keys[KeyEvent.VK_S] && cursor < 2){ // down
			cursor += 1;
		}
		if (keys[KeyEvent.VK_J]){ // confirmed
			userPicked = cursor + 1;
		}
	}
	
	// keyboard methods from JPanel
	public void keyTyped(KeyEvent e){}
	
	public void keyPressed(KeyEvent e){
		keys[e.getKeyCode()] = true;
	}
	public void keyReleased(KeyEvent e){
		keys[e.getKeyCode()] = false;
	}
	
	public int picked(){ // allows FireEmblem class to check status of user selection
		return userPicked;
	}
	
	private void drawCursor(Graphics g){
		g.setColor(new Color(255,255,255)); // colour -> white
		g.fillRect(57,231+63*cursor,16,16); // draws cursor; selections are 63 px apart on the y axis
	}
	
	private void drawOnMap(Graphics g){ // draws cursor effect on map
		g.drawImage(box[beat],boxX[cursor],boxY[cursor],this);
	}
	
	public void paintComponent(Graphics g){ // method adopted from JPanel, draws graphics into screen
		metronome(); // animation
		g.drawImage(back,0,0,this);
		drawCursor(g);
		drawOnMap(g);
	}
}

////// ACTUAL GAME

///// Drawer Master JPanel Class
class Drawer extends JPanel implements KeyListener{
	private boolean[]keys; // Array keeps track of which keys on the keyboard are being pressed
	private Terrain mapTrack;
	private int cx, cy, bx, by, rx, ry,bfx,bfy,rfx,rfy,bwx,bwy,rwx,rwy,bgrabbed,rgrabbed;	//neural cursor coordinates, blue cursor
//red cursor, blue focus point, red focus point, blue walked point, red walked point, blue int when placing units, same for red 
	private Unit bfocused,rfocused;	//blue focused unit in game, red focused unit in game
	private int turn = 0;	//0=blue turn ,1=red turn
	private int dayCount = 1;
	private Image[] cursor=new Image[2]; // Image Array of cursor used by player 1 and 2
	private Image[] bars=new Image[4]; // Array of information bars shown at bottom of screen
	private Image[] menus=new Image[2]; // images of menus for unit placement
	private Image[] liteTile=new Image[2]; // images used to highlight certain tiles
	private Image ok = new ImageIcon("Sprites/ok_highlight.png").getImage(); // highlighted OK button
	private Image ready = new ImageIcon("Sprites/ok_ready.png").getImage(); // ready button
	private int mode; //keeps track of which mode Drawer is currently in - 1=in game,2=unit positioning
	private File ttf; // stores TTF file for font
	private Font font; // font to draw Strings
	
	private int beat = 1; // used for animations - beat keeps track of which frame to use
	private int beatCount = 0; // counter; modded by 2 to determine if an action should continue
	private int beatDir = 1; // determines if beat is going up or down
	
	private ArrayList<String> p1,p2;	//taken from playerselect,converted to int[][], then Unit[][]
	private boolean p1ready,p2ready;	//flags when both teams are ready to fight

	private Image[][] overworld = new Image[11][3];	//Arraylist of overworld sprites
	
	private BufferedImage[][] overworld1 = new BufferedImage[11][3]; // player 1 overworld sprites; used during unit placement
	private BufferedImage[][] overworld2 = new BufferedImage[11][3]; // ditto but for player 2
	
	private Image[] portrait1=new Image[10]; // portraits of player 1 units;			<- these five are all taken in from FireEmblem
	private Image[] portrait2=new Image[10]; // ditto but for player 2
	private String[] roster; // names of units
	private int[][]unitPos=new int[15][20]; //int version of Units on map
	private Unit[][] units=new Unit[15][20]; //actual array of Units
	
	public Drawer(Terrain mappans,ArrayList<String> np1,ArrayList<String> np2,Image[]nportrait1,Image[]nportrait2,String[]nroster) {// constructor; takes in existing values from FireEmblem
		super();
		keys = new boolean[KeyEvent.KEY_LAST+1]; // starts up keyboard tracking
		mapTrack = mappans;
		setSize(803,669);
		cx = 0; // initial cursor position
		cy = 0;
		
		bx = 0; // cursor position for unit placement
		by = 0;
		rx = 19;
		ry = 14;
		
		bgrabbed=0; // keeps track of which units the players are currently holding for unit placement
		rgrabbed=0;
		bfocused=null; // keeps track of which units the players have selected during normal gameplay
		rfocused=null;
		
		mode=2; // initial mode is unit placement
		
		cursor[0] = new ImageIcon("Sprites/BlueSelector.png").getImage(); // gets cursor images
		cursor[1] = new ImageIcon("Sprites/RedSelector.png").getImage();
		
		bars[0]=(new ImageIcon("Sprites/bar_blue.png").getImage()); // gets infoboar images
		bars[1]=(new ImageIcon("Sprites/bar_red.png").getImage());
		bars[2]=(new ImageIcon("Sprites/bar_info.png").getImage());
		
		menus[0]=(new ImageIcon("Sprites/p1units.png").getImage()); // gets menu images
		menus[1]=(new ImageIcon("Sprites/p2units.png").getImage());
		
		liteTile[0]=(new ImageIcon("Sprites/space_blue.png").getImage()); // gets highlighted tile images
		liteTile[1]=(new ImageIcon("Sprites/space_red.png").getImage());

		p1=np1; // sets values from FireEmblem
		p2=np2;
		p1ready=false;
		p2ready=false;
		
		portrait1=nportrait1;
		portrait2=nportrait2;
		roster=nroster;
		
		ttf = new File("Minecraftia.ttf"); // grabs TTF file from folder

		try{
			font = Font.createFont(Font.TRUETYPE_FONT,ttf).deriveFont(Font.PLAIN,12); // turns it to usable font, size 12
		}
		catch(IOException ex){
			System.out.println("Achtung! Font nicht gefunden!");
		}
		catch(FontFormatException ex){
			System.out.println("Achtung! Font nicht gefunden!");
		}
		
		for (int b=1;b<11;b++){ // loads overworld images for player 1
			for(int i=1;i<4;i++){  // for each frame
				try{
					overworld1[b][i-1] = ImageIO.read(new File("Sprites/Overworld/" + roster[b-1] + "/1/Normal/" + roster[b-1].toLowerCase() + "1" + i + ".png"));
				}
				catch (IOException ex){
					System.out.println("Achtung!");
				}
			}
		}
		
		for (int r=1;r<11;r++){ // loads overworld images for player 2
			for(int i=1;i<4;i++){ // for each frame
				try{
					overworld2[r][i-1] = ImageIO.read(new File("Sprites/Overworld/" + roster[r-1] + "/2/Normal/" + roster[r-1].toLowerCase() + "2" + i + ".png"));
				}
				catch (IOException ex){
					System.out.println("Achtung!");
				}
			}
		}
		
		addKeyListener(this);
	}
	
	public void addNotify(){ // method inherited from JPanel
		super.addNotify();
		requestFocus(); // makes sure that Windows puts focus into this window
	}

	public void moveCursor(){ // method checks for keyboard input and does things accordingly
		if (mode == 2){ //these are commands for unit positioning
			if (!p1ready){ // while player 1 is picking
				if (keys[KeyEvent.VK_D] && bx < 11){ // right 
	                bx += 1; 
	                if (bx == 5){ // this makes the cursor jump to the unit placement menu
	                    bx = 7; 
	                    by = 1; 
	                } 
	            } 
	            if (keys[KeyEvent.VK_A] && bx > 0){ // left
	                bx -= 1; 
	                if (bx == 6){ // this makes the cursor jump to the grid area
	                    bx = 4; 
	                    by = 0; 
	                } 
	            } 
	            if (keys[KeyEvent.VK_W]){ // up
	                if (bx < 5 && by > 0){ // if within grid area
	                    by -= 1; 
	                } 
	                else if (bx > 6 && by > 1){  // if within unit placement menu
	                    by -= 1; 
	                } 
	            } 
	            if (keys[KeyEvent.VK_S]){ // down
	                if (bx < 5 && by < 4){  // if within grid area
	                    by += 1; 
	                } 
	                else if (bx > 6 && by < 4){  // if within unit placement menu
	                    by += 1; 
	                } 
	            } 
				if (keys[KeyEvent.VK_J]){ //grabs unit		
					if (bgrabbed==0){ // if not holding anything 
						if (by == 4 && bx > 6){ // and cursor is on ok button
							p1ready=true;
						}
						else{
							bgrabbed=unitPos[by][bx]; // grabs unit
							unitPos[by][bx]=0;
						}					
					}
				}
				if (keys[KeyEvent.VK_K]){		//places unit
					if (unitPos[by][bx]==0){
						unitPos[by][bx]=bgrabbed;
						bgrabbed=0;
					}
				}
			}
			
			if (!p2ready){ // see above, but with p2
				if (keys[KeyEvent.VK_RIGHT] && rx < 19){ // right
	                rx += 1; 
	                if (rx == 12){ // makes cursor jump to grid area
	                    rx = 15; 
	                    ry = 10; 
	                } 
	                
	            } 
	            if (keys[KeyEvent.VK_LEFT] && rx > 7){ // left
	                rx -= 1; 
	                if (rx == 14){ // makes cursor jump to unit placement area
	                    rx = 11; 
	                    ry = 8; 
	                } 
	                  
	                
	            } 
	            if (keys[KeyEvent.VK_UP]){ // up
	                if (rx < 12 && ry > 8){ // if in unit menu
	                    ry -= 1; 
	                } 
	                else if (rx > 14 && ry > 10){  // if in grid
	                    ry -= 1; 
	                } 
	                  
	               
	            } 
	            if (keys[KeyEvent.VK_DOWN]){ // down
	                if (rx < 12 && ry < 11){ // if in unit menu
	                    ry += 1; 
	                } 
	                else if (rx > 14 && ry < 14){ // if in grid
	                    ry += 1; 
	                } 
	  
	                
	            } 
				if (keys[KeyEvent.VK_NUMPAD1]){ // grab unit
					if (rgrabbed==0){  // if not holding anything
						if (ry == 11 && rx < 11){ // on ok button
							p2ready=true;
						}
						else{ // grab unit
							rgrabbed=unitPos[ry][rx];
							unitPos[ry][rx]=0;
						}				
					}
				}
				if (keys[KeyEvent.VK_NUMPAD2]){
					if (unitPos[ry][rx]==0){ // drop unit
						unitPos[ry][rx]=rgrabbed;
						rgrabbed=0;
					}
				}
			}
		}
		else{ //this is when in game
			if (turn == 0){
				if (keys[KeyEvent.VK_D] && cx < 19){ // right
					cx += 1;
				}
				if (keys[KeyEvent.VK_A] && cx > 0){ // left
					cx -= 1;
				}
				if (keys[KeyEvent.VK_W] && cy > 0){ // up
					cy -= 1;
				}
				if (keys[KeyEvent.VK_S] && cy < 14){ // down
					cy += 1;
				}
				if (keys[KeyEvent.VK_J]){		//the confirm button
					if(bfocused==null){			//if nothing is focused, focus on an available unit
							if (units[cy][cx]!=null&&units[cy][cx].getTeam()==1&&units[cy][cx].getPhase()==0){
							bfocused=units[cy][cx];
							setFocusXY1(cx,cy);						
						}						
					}					
					else{			//if a unit is focused,pick a place to move
						if(bfocused.getPhase()==0&&withinRange(bfocused,bfx,bfy,bfocused.getMov())){
							units[bfy][bfx]=null; // remove unit from old lcoation and place on new location
							units[cy][cx]=bfocused;
							bwx=cx;
							bwy=cy;
							bfocused.setPhase(1);
						}
						else if(bfocused.getName().equals("Cleric")&&bfocused.getPhase()==1&&units[cy][cx]!=null&&withinAttackRange(bfocused,bwx,bwy,bfocused.getAttackRange(),units[cy][cx])){ // highlights areas for cleric to heal
							bfocused.heal(units[cy][cx]);		//special case for cleric, who heal instead of attack
							bfocused.setPhase(2);		//action deactivated unit
							bfocused=null;				//releases focus
						}
						else if(bfocused.getPhase()==1&&units[cy][cx]!=null&&withinAttackRange(bfocused,bwx,bwy,bfocused.getAttackRange(),units[cy][cx])){
							if(bfocused.attack(units[cy][cx],cx,cy)==false){	//attacking enemy
								units[cy][cx]=null;
							}
							bfocused.setPhase(2);	
							bfocused=null;
						}
						else if(bfocused.getPhase()==1&&bwx==cx&&bwy==cy){
							bfocused.setPhase(2);		//action deactivated unit
							bfocused=null;				//releases focus
						}
					}
				}
				if (keys[KeyEvent.VK_K]){	//the back button
					if(bfocused!=null){
						if (bfocused.getPhase()==1){	//if unit already moved
							bfocused.setPhase(0);		//go back to original spot and try again
							units[bwy][bwx]=null;
							units[bfy][bfx]=bfocused;
						}
						if(bfocused.getPhase()==0){		//release focus
							bfocused=null;
						}
					}		
				}
			}
			else if (turn == 1){		//same for p2
				if (keys[KeyEvent.VK_RIGHT] && cx < 19){ // right
					cx += 1;
				}
				if (keys[KeyEvent.VK_LEFT] && cx > 0){ // left
					cx -= 1;
				}
				if (keys[KeyEvent.VK_UP] && cy > 0){ // up
					cy -= 1;
				}
				if (keys[KeyEvent.VK_DOWN] && cy < 14){ // down
					cy += 1;
				}
				if (keys[KeyEvent.VK_NUMPAD1]){
					if(rfocused==null){ // if nothing is focused, focus on selected unit
						if (units[cy][cx]!=null&&units[cy][cx].getTeam()==2&&units[cy][cx].getPhase()==0){
						rfocused=units[cy][cx];
						setFocusXY2(cx,cy);						
						}						
					}					
					else{
						if(rfocused.getPhase()==0&&withinRange(rfocused,rfx,rfy,rfocused.getMov())){ // if a unit is focused, pick place to move
							units[rfy][rfx]=null; // remove from old position and add to new position
							units[cy][cx]=rfocused;
							rwx=cx;
							rwy=cy;
							rfocused.setPhase(1);
						}
						else if(rfocused.getName().equals("Cleric")&&rfocused.getPhase()==1&&units[cy][cx]!=null&&withinAttackRange(rfocused,rwx,rwy,rfocused.getAttackRange(),units[cy][cx])){ // for clerics to heal units
							rfocused.heal(units[cy][cx]);
							rfocused.setPhase(2);
							rfocused=null;
						}
						else if(rfocused.getPhase()==1&&units[cy][cx]!=null&&withinAttackRange(rfocused,rwx,rwy,rfocused.getAttackRange(),units[cy][cx])){ // attacking enemies
							if(rfocused.attack(units[cy][cx],cx,cy)==false){
								units[cy][cx]=null;
							}
							rfocused.setPhase(2);
							rfocused=null;
						}
						else if(rfocused.getPhase()==1&&rwx==cx&&rwy==cy){ // deactivate unit; remove focus
							rfocused.setPhase(2);
							rfocused=null;
						}
					}
				}
				if (keys[KeyEvent.VK_NUMPAD2]){ // back button; restores unit to original location
					if(rfocused!=null){
						if (rfocused.getPhase()==1){
							rfocused.setPhase(0);
							units[rwy][rwx]=null;
							units[rfy][rfx]=rfocused;
						}
						if(rfocused.getPhase()==0){
							rfocused=null;
						}
					}
				}
			}
		}
	}

	// keyboard methods from JPanel
	public void keyTyped(KeyEvent e){}
	
	public void keyPressed(KeyEvent e){
		keys[e.getKeyCode()] = true;
	}
	public void keyReleased(KeyEvent e){
		keys[e.getKeyCode()] = false;
	}
	
	public void drawCursor(Graphics g){ // draws player cursor for normal gameplay; colour depends on which player's turn it is
		g.drawImage(cursor[turn],cx*40,cy*40,null);
	}
	
	public void drawSpecCursor(int turn,int x,int y,Graphics g){ // draws cursor for unit placement mode
		Graphics2D comp2D = (Graphics2D) g;
        if (turn == 0){ // for player 1
            if (y == 4 && x > 6){ // if cursor is on ok button
            	if (!p1ready){ // if not yet ready
            		g.drawImage(ok,343,184,null); 
            	}
                else{ // if ready
                	g.drawImage(ready,343,184,null); 
                }
            } 
            else{ // if not, draw normal cursor
                g.drawImage(cursor[0],x*40,y*40,null); 
                if (bgrabbed != 0){ // if unit is currently held; draw unit along with cursor
                	BufferedImage sprite = overworld1[bgrabbed][beat];
					int h = sprite.getHeight();
					int w = sprite.getWidth();
					comp2D.drawImage(sprite,x*40+20-(h/2),y*40+20-(w/2),this); // centers unit on grid
                }
            }
        } 
        else if (turn == 1){ // player 2
            if (y == 11 && x < 12){ // if cursor is on ok button
            	if (!p2ready){ // if not ready
            		g.drawImage(ok,280,464,null); 
            	}
                else{ // if ready
                	g.drawImage(ready,280,464,null); 
                }
            } 
            else{ // if not, draw normal cursor
                g.drawImage(cursor[1],x*40,y*40,null);
                if (rgrabbed != 0){ // if unit is currently held, draw unit along with cursor
                	BufferedImage sprite = overworld2[rgrabbed][beat];
					int h = sprite.getHeight();
					int w = sprite.getWidth();
					comp2D.drawImage(sprite,x*40+20-(h/2),y*40+20-(w/2),this); // centers unit on grid
                }
            } 
        } 
    } 
	
	private void blueTiles(Graphics g){ // draws p1 grid area during unit placement
		for (int x=0;x<5;x++){
			for (int y=0;y<5;y++){
				g.drawImage(liteTile[0],x*40,y*40,null);
			}
		}
	}
	
	private void redTiles(Graphics g){ // draws p2 grid area during unit placement
		for (int x=15;x<20;x++){
			for (int y=10;y<15;y++){
				g.drawImage(liteTile[1],x*40,y*40,null);
			}
		}
	}
	
	//This method displays how far the unit can move using a recursive search.
	private void displayRange(Unit u,int x,int y,Graphics g){
		int[][] done=new int[15][20];			//used for finding most efficient path
		g.drawImage(liteTile[0],x*40,y*40,null);
		done[y][x]=u.getMov();
		displayRange(u,x,y,u.getMov(),done,g);	//call recursive part of method
		
	}
	private void displayRange(Unit u,int x,int y,int range,int[][] done,Graphics g){
		if(u.getName().equals("Pegasus")||u.getName().equals("Wyvern")){	//flying units are unaffected by terrain
			if(range>=1){
				if (x<19&&done[y][x+1]<range){		//if a more efficient path hasnt been taken yet...		
					g.drawImage(liteTile[0],(x+1)*40,y*40,null);  //go right,left,down,up, each time subtracting one step each time
					done[y][x+1]=range-1;
					displayRange(u,x+1,y,range-1,done,g);
				}
				if (x>0&&done[y][x-1]<range){
					g.drawImage(liteTile[0],(x-1)*40,y*40,null);
					done[y][x-1]=range-1;
					displayRange(u,x-1,y,range-1,done,g);
				}
				if (y<14&&done[y+1][x]<range){
					g.drawImage(liteTile[0],x*40,(y+1)*40,null);
					done[y+1][x]=range-1;
					displayRange(u,x,y+1,range-1,done,g);
				}
				if (y>0&&done[y-1][x]<range){
					g.drawImage(liteTile[0],x*40,(y-1)*40,null);
					done[y-1][x]=range-1;
					displayRange(u,x,y-1,range-1,done,g);
				}
			}
		}
		else{		//for all ground units
			if(range>=2){
				if (mapTrack.get(x+1,y)==3&&x<19&&done[y][x+1]<range){	//forest (mapTrack.get() == 3) takes twice the amount of steps; refer to Terrain for more details
					g.drawImage(liteTile[0],(x+1)*40,y*40,null); // operation similar to previous if statement
					done[y][x+1]=range-2;
					displayRange(u,x+1,y,range-2,done,g);
				}
				if (mapTrack.get(x-1,y)==3&&x>0&&done[y][x-1]<range){
					g.drawImage(liteTile[0],(x-1)*40,y*40,null);
					done[y][x-1]=range-2;
					displayRange(u,x-1,y,range-2,done,g);
				}
				if (mapTrack.get(x,y+1)==3&&y<14&&done[y+1][x]<range){
					g.drawImage(liteTile[0],x*40,(y+1)*40,null);
					done[y+1][x]=range-2;
					displayRange(u,x,y+1,range-2,done,g);
				}
				if (mapTrack.get(x,y-1)==3&&y>0&&done[y-1][x]<range){
					g.drawImage(liteTile[0],x*40,(y-1)*40,null);
					done[y-1][x]=range-2;
					displayRange(u,x,y-1,range-2,done,g);
				}
			}				
			if(range>=1){	//cannot go on mountains (mapTrack = 4) or water (mapTrack = 2); refer to Terrain for more details
				if (mapTrack.get(x+1,y)!=2&&mapTrack.get(x+1,y)!=3&&mapTrack.get(x+1,y)!=4&&x<19&&done[y][x+1]<range){
					g.drawImage(liteTile[0],(x+1)*40,y*40,null);
					done[y][x+1]=range-1;
					displayRange(u,x+1,y,range-1,done,g);
				}
				if (mapTrack.get(x-1,y)!=2&&mapTrack.get(x-1,y)!=3&&mapTrack.get(x-1,y)!=4&&x>0&&done[y][x-1]<range){
					g.drawImage(liteTile[0],(x-1)*40,y*40,null);
					done[y][x-1]=range-1;
					displayRange(u,x-1,y,range-1,done,g);
				}
				if (mapTrack.get(x,y+1)!=2&&mapTrack.get(x,y+1)!=3&&mapTrack.get(x,y+1)!=4&&y<14&&done[y+1][x]<range){
					g.drawImage(liteTile[0],x*40,(y+1)*40,null);
					done[y+1][x]=range-1;
					displayRange(u,x,y+1,range-1,done,g);
				}
				if (mapTrack.get(x,y-1)!=2&&mapTrack.get(x,y-1)!=3&&mapTrack.get(x,y-1)!=4&&y>0&&done[y-1][x]<range){
					g.drawImage(liteTile[0],x*40,(y-1)*40,null);
					done[y-1][x]=range-1;
					displayRange(u,x,y-1,range-1,done,g);
				}
			}
		}		
	}
	//This method determines if a unit can go to a certain spot or no. It mimics the technique of displayRange()
	private boolean withinRange(Unit u,int x,int y,int range){
		int[][] done=new int[15][20];
		done[y][x]=u.getMov();				
		return withinRange(u,x,y,u.getMov(),done);								
	}
	private boolean withinRange(Unit u,int x,int y,int range,int[][] done){
		if (cx==x&&cy==y&&units[cy][cx]==null){		//if the spot is found, return true
			return true;
		}
		else{			//same as displayRange()
			boolean can=false;
			if(u.getName().equals("Pegasus")||u.getName().equals("Wyvern")){ // bypasses Terain if Pegasus or Wyvern (flying units)
				if(range>=1){
					if (x<19&&done[y][x+1]<range){ // right, left, down, up
						done[y][x+1]=range-1;
						if(withinRange(u,x+1,y,range-1,done)){
							can=true;
						}
					}
					if (x>0&&done[y][x-1]<range){
						done[y][x-1]=range-1;
						if(withinRange(u,x-1,y,range-1,done)){
							can=true;
						}
					}
					if (y<14&&done[y+1][x]<range){
						done[y+1][x]=range-1;
						if( withinRange(u,x,y+1,range-1,done)){
							can=true;
						}
					}
					if (y>0&&done[y-1][x]<range){
						done[y-1][x]=range-1;
						if (withinRange(u,x,y-1,range-1,done)){
							can=true;
						}						
					}					
				}
			}
			else{
				if(range>=2){				
					if (mapTrack.get(x+1,y)==3&&x<19&&done[y][x+1]<range){ // range for ground units; takes into account forest
						done[y][x+1]=range-2; // right left, down, up
						if(withinRange(u,x+1,y,range-2,done)){
							can=true;
						}
					}
					if (mapTrack.get(x-1,y)==3&&x>0&&done[y][x-1]<range){
						done[y][x-1]=range-2;
						if(withinRange(u,x-1,y,range-2,done)){
							can=true;
						}
					}
					if (mapTrack.get(x,y+1)==3&&y<14&&done[y+1][x]<range){
						done[y+1][x]=range-2;
						if(withinRange(u,x,y+1,range-2,done)){
							can=true;
						}
					}
					if (mapTrack.get(x,y-1)==3&&y>0&&done[y-1][x]<range){
						done[y-1][x]=range-2;
						if(withinRange(u,x,y-1,range-2,done)){
							can=true;
						}
					}
				}				
				if(range>=1){ // takes care of other Terrain tiles
					if (mapTrack.get(x+1,y)!=2&&mapTrack.get(x+1,y)!=3&&mapTrack.get(x+1,y)!=4&&x<19&&done[y][x+1]<range){
						done[y][x+1]=range-1;
						if(withinRange(u,x+1,y,range-1,done)){
							can=true;
						}
					}
					if (mapTrack.get(x-1,y)!=2&&mapTrack.get(x-1,y)!=3&&mapTrack.get(x-1,y)!=4&&x>0&&done[y][x-1]<range){
						done[y][x-1]=range-1;
						if(withinRange(u,x-1,y,range-1,done)){
							can=true;
						}
					}
					if (mapTrack.get(x,y+1)!=2&&mapTrack.get(x,y+1)!=3&&mapTrack.get(x,y+1)!=4&&y<14&&done[y+1][x]<range){
						done[y+1][x]=range-1;
						if( withinRange(u,x,y+1,range-1,done)){
							can=true;
						}
					}
					if (mapTrack.get(x,y-1)!=2&&mapTrack.get(x,y-1)!=3&&mapTrack.get(x,y-1)!=4&&y>0&&done[y-1][x]<range){
						done[y-1][x]=range-1;
						if (withinRange(u,x,y-1,range-1,done)){
							can=true;
						}						
					}					
				}
			}			
			return can;	
		}		
	}
	//This method display the range that the unit can attack. Since the cases are so few, we can manually go through them
	private void displayAttackRange(Unit u,int x,int y,Graphics g){
		int attrange=u.getAttackRange();
		if (attrange==1){		//melee units
			g.drawImage(liteTile[1],(x+1)*40,y*40,null);
			g.drawImage(liteTile[1],(x-1)*40,y*40,null);
			g.drawImage(liteTile[1],x*40,(y+1)*40,null);
			g.drawImage(liteTile[1],x*40,(y-1)*40,null);
		}
		if(attrange==2){		//ranged units
			g.drawImage(liteTile[1],(x+2)*40,y*40,null);
			g.drawImage(liteTile[1],(x-2)*40,y*40,null);
			g.drawImage(liteTile[1],x*40,(y+2)*40,null);
			g.drawImage(liteTile[1],x*40,(y-2)*40,null);
			g.drawImage(liteTile[1],(x+1)*40,(y+1)*40,null);
			g.drawImage(liteTile[1],(x-1)*40,(y+1)*40,null);
			g.drawImage(liteTile[1],(x+1)*40,(y-1)*40,null);
			g.drawImage(liteTile[1],(x-1)*40,(y-1)*40,null);
		}
		if(attrange==12){		//sages that can hit from afar and close combat
			g.drawImage(liteTile[1],(x+1)*40,y*40,null);
			g.drawImage(liteTile[1],(x-1)*40,y*40,null);
			g.drawImage(liteTile[1],x*40,(y+1)*40,null);
			g.drawImage(liteTile[1],x*40,(y-1)*40,null);
			g.drawImage(liteTile[1],(x+2)*40,y*40,null);
			g.drawImage(liteTile[1],(x-2)*40,y*40,null);
			g.drawImage(liteTile[1],x*40,(y+2)*40,null);
			g.drawImage(liteTile[1],x*40,(y-2)*40,null);
			g.drawImage(liteTile[1],(x+1)*40,(y+1)*40,null);
			g.drawImage(liteTile[1],(x-1)*40,(y+1)*40,null);
			g.drawImage(liteTile[1],(x+1)*40,(y-1)*40,null);
			g.drawImage(liteTile[1],(x-1)*40,(y-1)*40,null);
		}
	}
	//This method is the counterpart of displayAttackRange(), which tells if you can attack a certain eneny or not
	private boolean withinAttackRange(Unit u,int x,int y,int attrange,Unit enemy){
		boolean can=false;
		if(u.getName().equals("Cleric")){		//special case for clerics that heal instead of attack
			if(u.getTeam()==enemy.getTeam()){
				if(cx==x+1&&cy==y||cx==x-1&&cy==y||cx==x&&cy==y+1||cx==x&&cy==y-1){ // checks adjacent non-diagonal Tiles for allied units
					return true;
				}
				else{
					return false;
				}
			}
			else{
				return false;
			}
		}
		else{
			if(u.getTeam()!=enemy.getTeam()){ // ditto as above, but for other units attacking enemy units
				if (attrange==1){ // melee units
					if (cx==x+1&&cy==y){can=true;}
					if (cx==x-1&&cy==y){can=true;}
					if (cx==x&&cy==y+1){can=true;}
					if (cx==x&&cy==y-1){can=true;}
				}
				if(attrange==2){ // range units
					if (cx==x+2&&cy==y){can=true;}
					if (cx==x-2&&cy==y){can=true;}
					if (cx==x&&cy==y+2){can=true;}
					if (cx==x&&cy==y-2){can=true;}
					if (cx==x+1&&cy==y+1){can=true;}
					if (cx==x-1&&cy==y+1){can=true;}
					if (cx==x+1&&cy==y-1){can=true;}
					if (cx==x-1&&cy==y-1){can=true;}
				}
				if(attrange==12){ // both
					if (cx==x+1&&cy==y){can=true;}
					if (cx==x-1&&cy==y){can=true;}
					if (cx==x&&cy==y+1){can=true;}
					if (cx==x&&cy==y-1){can=true;}
					if (cx==x+2&&cy==y){can=true;}
					if (cx==x-2&&cy==y){can=true;}
					if (cx==x&&cy==y+2){can=true;}
					if (cx==x&&cy==y-2){can=true;}
					if (cx==x+1&&cy==y+1){can=true;}
					if (cx==x-1&&cy==y+1){can=true;}
					if (cx==x+1&&cy==y-1){can=true;}
					if (cx==x-1&&cy==y-1){can=true;}
				}
			return can;
			}
			else{
				return false;
			}				
		}
		
	}
	
	private void drawBar(Graphics g){ // draws information bar at bottom of screen
		Graphics2D comp2D = (Graphics2D)g; // used to draw fonts
		comp2D.setColor(new Color(255,255,255)); // sets font
		comp2D.setFont(font);
		
		if (mode == 2){ // if in unit placement, draw unit placement bar
			g.drawImage(bars[2],0,600,null);
		}
		else{ // draw bar depending on who's turn it is
			g.drawImage(bars[turn],0,600,null);
			if (units[cy][cx] != null){ // if location has a unit on it
				if (units[cy][cx].getTeam() == 1){ // if unit is p1
					int index=0; // goes through each portrait until one matching location of unit's name in roster is found
					for (int i=0;i<roster.length;i++){
						if(units[cy][cx].getName().equals(roster[i])){
							index=i;
						}
					}
					g.drawImage(portrait1[index],9,600,this);
				}
				else{ // if unit is p2; process is the same
					int index=0;
					for (int i=0;i<roster.length;i++){
						if(units[cy][cx].getName().equals(roster[i])){
							index=i;
						}
					}
					g.drawImage(portrait2[index],9,600,this);
				}
				comp2D.drawString("HP: " + units[cy][cx].getHP() + "/" + units[cy][cx].getMaxHP(),66,622);
			}
			comp2D.drawString(mapTrack.getDef(cx,cy) + " (" + mapTrack.getName(cx,cy) + ")",279,622);;
			comp2D.drawString("" + dayCount,650,622);
		}
	}
	
	////////////////////////////Access Methods//////////////////////////////
	private void setFocusXY1(int x,int y){
		bfx=x;
		bfy=y;
	}
	
	private void setFocusXY2(int x,int y){
		rfx=x;
		rfy=y;
	}
	
	private void unitMenus(Graphics g){ // draws unit menus for unit placement
        g.drawImage(menus[0],240,29,null); 
        g.drawImage(menus[1],264,308,null); 
    } 
    	
	public boolean isPicking(){ // used to give status of Drawer to FireEmblem
		return !(p1ready && p2ready);
	}
	
	public void setMode(int i){ // sets mode
		mode=i;
	}
	
	public int getMode(){ // used to get mode for FireEmblem
		return mode;
	}
	
	public int[][] getUnitPos(){ // used to hand over unitPos to FireEmblem for conversion
		return unitPos;
	}
	
	public void setUnit(Unit[][] nunits){ // used to get units from FireEmblem convert
		units=nunits;
	}
	
	//This method checks if one player has routed the other and thus, win the game
	public int checkWin(){
		boolean p1Win=true; // flags
		boolean p2Win=true;
		for(int r=0;r<15;r++){ // goes through each tile
			for(int c=0;c<20;c++){
				if(units[r][c]!=null){
					if(units[r][c].getTeam()==1){ // if units from team 1 are found, p2 has not won yet
						p2Win=false;
					}
					if(units[r][c].getTeam()==2){ // same for p1
						p1Win=false;
					}
				}
			}
		}
		if(p1Win){ // used to give status back to FireEmblem
			return 1;
		}
		else if(p2Win){
			return 2;
		}
		else{
			return 0;
		}
	}
	//This method allows player to take turns moving their units. It checks if the player that has his turn has finished using all his units.
	//If so, the turn switches and the units are refreshed
	public void switchTurn(){
		boolean change=true;
		for (int r=0;r<15;r++){
			for(int c=0;c<20;c++){
				if (units[r][c]!=null&&units[r][c].getTeam()==(turn+1)&&units[r][c].getPhase()!=2){
					change=false;
				}
			}
		}
		if (change){
			if(turn==0){
				turn=1;
				for (int r=0;r<15;r++){
					for(int c=0;c<20;c++){ // resets all p2 units
						if (units[r][c]!=null&&units[r][c].getTeam()==2&&units[r][c].getPhase()==2){
							units[r][c].setPhase(0);
						}
					}
				}
			}
			else if(turn==1){
				turn=0;
				for (int r=0;r<15;r++){ // resets all p1 units
					for(int c=0;c<20;c++){
						if (units[r][c]!=null&&units[r][c].getTeam()==1&&units[r][c].getPhase()==2){
							units[r][c].setPhase(0);
						}
					}
				}
				dayCount += 1; // once p2 is done, the "day" is over, which brings the count up by one
			}
		}
	}
	
	public void placeUnits(){ // used to place units on to grid when initializing drawer for unit placement
		int index1=0;
		for (int r=0;r<3;r++){ // p1
			for (int c=0;c<5;c++){
				while(index1<p1.size()){
					for(int i=0;i<roster.length;i++){
						if(roster[i].equals(p1.get(index1))){
							unitPos[1+r][7+c]=i+1;
							break;
						}
					}
					break;
				}
				index1+=1;				
			}			
		}
		int index2=0;
		for (int r=0;r<3;r++){ // p2
			for (int c=0;c<5;c++){
				while(index2<p2.size()){
					for(int i=0;i<roster.length;i++){
						if(roster[i].equals(p2.get(index2))){
							unitPos[8+r][7+c]=i+1;
							break;
						}
					}
					break;
				}
				index2+=1;				
			}			
		}
	}
	
	private void metronome(){ // used for animations
		if (beatCount > 1000){ // prevents counter from getting to big
			beatCount = 0;
		}
		else{
			beatCount += 1;
		}
		
		if (beatCount % 2 == 0){ // if counter divisible by 2
			if (beat == 2){ // switch directions if beat is max
				beatDir = -1;
			}
			else if (beat == 0){ //switch directions if beat is min
				beatDir = 1;
			}
			beat += 1 * beatDir;
		}
	}
	
	private void drawTempUnits(Graphics2D comp2D){ // draws units during unit placement
		for(int r=0;r<15;r++){ // goes through each tile
			for(int c=0;c<20;c++){
				if (unitPos[r][c] != 0){
					if (r < 5){ // if unit is found on top half of screen, p1
						BufferedImage sprite = overworld1[unitPos[r][c]][beat];
						int h = sprite.getHeight();
						int w = sprite.getWidth();
						comp2D.drawImage(sprite,c*40+20-(int)(h/2.0),r*40+20-(int)(w/2.0),this); // centers unit on tile
					}
					else{ // else, p2
						BufferedImage sprite = overworld2[unitPos[r][c]][beat];
						int h = sprite.getHeight();
						int w = sprite.getWidth();
						comp2D.drawImage(sprite,c*40+20-(int)(h/2.0),r*40+20-(int)(w/2.0),this); // centers unit on tile
					}
				}
			}
		}
	}
	
	private void drawUnits(Graphics2D comp2D){ // draws units during gameplay
		for(int r=0;r<15;r++){
			for(int c=0;c<20;c++){ // goes through each tile
				if (units[r][c] != null){ // if tile has a unit in it
					BufferedImage sprite = null;
					if (units[r][c].getPhase() == 1 || units[r][c].equals(bfocused)){ // if unit is in phase 1 or is selected
						sprite = units[r][c].getImage("focus",beat);
					}
					else if (units[r][c].getPhase() == 0){ // if unit is in normal move
						sprite = units[r][c].getImage("normal",beat);
					}
					else if (units[r][c].getPhase() == 2){ // if a unit is inactive
						sprite = units[r][c].getImage("inactive",beat);
					}
					int h = sprite.getHeight();
					int w = sprite.getWidth();
					comp2D.drawImage(sprite,c*40+20-(int)(h/2.0),r*40+20-(int)(w/2.0),this); // centers image
				}
			}
		}
	}
	
	public void paintComponent(Graphics g){ // method adopted from JPanel, draws graphics into screen
		metronome();// animation
		
		Graphics2D comp2D = (Graphics2D) g;
		mapTrack.draw(g); // draws terrain
		
		if (mode == 2){ // while placing units
			blueTiles(g); // highlight tiles
			redTiles(g);
			unitMenus(g); // draw menu
			drawTempUnits(comp2D); // draw units
			drawSpecCursor(0,bx,by,g); // draw cursors
			drawSpecCursor(1,rx,ry,g);
		}
		else{ // normal gameplay
			if(turn==0){ // p1
				if (bfocused!=null){// draws movement range of selected unit
					if(bfocused.getPhase()==0){
						displayRange(bfocused,bfx,bfy,g);
					}
					if(bfocused.getPhase()==1){// draws attack range of selected unit
						displayAttackRange(bfocused,bwx,bwy,g);
					}
					
				}
			}
			if(turn==1){ // p2
				if (rfocused!=null){ // draws movement range of selected unit
					if(rfocused.getPhase()==0){
						displayRange(rfocused,rfx,rfy,g);
					}
					if(rfocused.getPhase()==1){ // draws attack tange of selected unit
						displayAttackRange(rfocused,rwx,rwy,g);
					}
					
				}
			}
			drawUnits(comp2D); // draws actual units
			drawCursor(g); // draws cursor
		}
		drawBar(g); // draws infobar
	}

}

///// Terrain Class
///// Keeps track of terrain tiles and defence values being used.
class Terrain{
	private int[][] map = new int[15][20]; // 2D Array used to keep track of terrain values
	private ArrayList<Image> fieldTiles = new ArrayList<Image>(); // ArrayList holds Images of each type of Terrain
	private String[] names = {"Plains","Road","Sea","Forest","Mountain","House","Castle","House","House","House","HQ","HQ"}; // names of each Terrain, corresponding to field tiles
	private int[] defVals = {0,0,0,2,3,2,3,2,2,2,3,3}; // defence value for each Terrain, corresponding to field tiles
	private int[] avoidVals = {0,0,-1,20,30,20,30,20,20,20,30,30}; // avoid value for each Terrain, corresponding to field tiles
	private Scanner mapFile; // reads through map files
	
	private Image bridge1 = new ImageIcon("Sprites/Terrain/bridge1.png").getImage(); // get pictures of bridges (used for drawRoad method)
	private Image bridge2 = new ImageIcon("Sprites/Terrain/bridge2.png").getImage();
	
	public Terrain(int mapName){
		try{
			mapFile = new Scanner(new BufferedReader(new FileReader("Maps/" + mapName + ".fem")));	// opens selected mapfile
		}
		catch(FileNotFoundException ex){
			System.out.println("Achtung!");
		}
		for (int r=0;r<15;r++){ // read each line on map file and split into Array
			String readRow = mapFile.nextLine();
			String[]row = readRow.split(" ");
			for (int c=0;c<20;c++){
				map[r][c] = Integer.parseInt(row[c]);
			}
		}
		mapFile.close();
		
		fieldTiles.add(new ImageIcon("Sprites/Terrain/plain.png").getImage()); // load Terrain tiles
		fieldTiles.add(new ImageIcon("Sprites/Terrain/road.png").getImage());
		fieldTiles.add(new ImageIcon("Sprites/Terrain/water1.png").getImage());
		fieldTiles.add(new ImageIcon("Sprites/Terrain/forest.png").getImage());
		fieldTiles.add(new ImageIcon("Sprites/Terrain/mountain.png").getImage());
		fieldTiles.add(new ImageIcon("Sprites/Terrain/camp.png").getImage());
		fieldTiles.add(new ImageIcon("Sprites/Terrain/castle.png").getImage());
		fieldTiles.add(new ImageIcon("Sprites/Terrain/house.png").getImage());
		fieldTiles.add(new ImageIcon("Sprites/Terrain/itemShop.png").getImage());
		fieldTiles.add(new ImageIcon("Sprites/Terrain/weaponShop.png").getImage());
		fieldTiles.add(new ImageIcon("Sprites/Terrain/command2.png").getImage());
		fieldTiles.add(new ImageIcon("Sprites/Terrain/command.png").getImage());

	}
	
	public int get (int x, int y){ // used to obtain values without going out of bounds
		if (0 <= x && x <= 19 && 0 <= y && y <= 14){
			return map[y][x];
		}
		else{
			return -1;
		}
	}
	
	public String getName(int x,int y){ // returns name of Terrain tile at (x,y)
		return names[map[y][x]];
	}
	
	public int getDef(int x,int y){ // returns defence value of Terrain tile at (x,y)
		return (defVals[map[y][x]]);
	}
	
	public int getAvoid(int x,int y){ // returns avoid value of Terrain tile at (x,y)
		return (avoidVals[map[y][x]]);
	}
	
	public Image getTile(int x,int y){ // returns picture of Terrain tile at (x,y)
		return fieldTiles.get(map[y][x]);
	}
	
	private void drawRoad(Graphics g,int x, int y){ // used to handle special cases for drawing roads
		if (get(x,y+1) == 2 && get(x,y-1) == 2){ // if the tiles above and below the current tile are water tiles, draw a west-east bridge
			g.drawImage(bridge1,x*40,y*40,null);
		}
		else if (get(x+1,y) == 2 && get(x-1,y) == 2){ // if the tiles are to the left and right of the current tile are water tiles, draw a north-south bridge
			g.drawImage(bridge2,x*40,y*40,null);
		}
		else{ // draw a normal road
			g.drawImage(fieldTiles.get(1),x*40,y*40,null);
		}
	}
	
	public void draw(Graphics g){ // draws all tiles in the 2D Array
		for (int r=0;r<15;r++){
			for (int c=0;c<20;c++){
				int obtenir = map[r][c];
				if (obtenir == 1){
					drawRoad(g,c,r);
				}
				else{
					g.drawImage(fieldTiles.get(obtenir),c*40,r*40,null);
				}
				
			}
		}
	}
}

///// Winner JPanel
///// Displays an image indicating who won the game.
class Winner extends JPanel implements KeyListener{
	private boolean[]keys; // Array of booleans indicating which keys have been pressed
	private Image back; // background - image depends on winner
	private boolean wereDoneHere = false; // flag if winning user has pressed their confirm button
	private int whoIsWinrar; // keeps track of who won the game; taken from Drawer

	public Winner(int whoWon){ // constructor; takes in which player won from Drawer
		super();
		keys = new boolean[KeyEvent.KEY_LAST+1];
		whoIsWinrar = whoWon;
		if (whoIsWinrar == 1){
			back = new ImageIcon("Sprites/p1win.jpg").getImage(); // if p1 won
		}
		else{ // if p2 won
			back = new ImageIcon("Sprites/p2win.jpg").getImage();
		}
		setSize(802,669);
		addKeyListener(this);
	}
	
	public void addNotify(){ // method inherited from JPanel
		super.addNotify();
		requestFocus(); // makes sure that Windows puts focus into this window
	}
	
	public void move(){ // method checks for keyboard input and does things accordingly
		if (whoIsWinrar == 1 && keys[KeyEvent.VK_J]){
			wereDoneHere = true;
		}
		if (whoIsWinrar == 2 && keys[KeyEvent.VK_NUMPAD1]){
			wereDoneHere = true;
		}
	}
	
	// keyboard methods from JPanel
	public void keyTyped(KeyEvent e){}
	
	public void keyPressed(KeyEvent e){
		keys[e.getKeyCode()] = true;
	}
	public void keyReleased(KeyEvent e){
		keys[e.getKeyCode()] = false;
	}
	
	public boolean done(){ // used to inform FireEmblem if user has made a selection or not
		return wereDoneHere;
	}
	
	public void paintComponent(Graphics g){ // method adopted from JPanel, draws graphics into screen
		g.drawImage(back,0,0,this);
	}
}