///// Unit Class
///// Used to create Unit objects; keeps track of unit stats and holds images used for animation.

// Kevin's Notes:
//http://serenesforest.net/fe7/class_base.htm
//ATTACK RANGE
//Physical Attack = (Strength+Weapon Might)*Weapon Triangle Bonus(1.2) DONE
//Attack Speed = Speed - Weapon Weight
//Double Attack = if(Speed >= (Enemy Speed + 3)) DONE
//AS = Speed - (Weapon Weight - Con)

//Hit Rate = 70 + Skill + Luck	DONE
//Evade = Attack Speed + Luck + Terrain Bonus	DONE
//Accuracy = Hit Rate (Attacker) - Evade (Defender) + Triangle Bonus	DONE

//DP = Terrain Bonus + Defense	
//Critical Rate = Weapon Critical + Skill / 2	DONE
//DEATH
//CAPS ON VALUES
//X,Y (20,15)

///// Modules
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.imageio.*;
import java.util.*;
import java.io.*;

class Unit{
	private String name; // name of unit
	private int hp,maxhp,att,skl,spd,def,con,mov,attrange,luc,team;	//hit points, attack,skill,speed,defense,resistence,constitution,movement,luck
	private Terrain map; // Terrain used in game; used in Unit to get defence and avoid values
	private int phase; //0 totally available,1 is have walked,2 is have used action(rescue,attack,buy) and is inactive
	
	private BufferedImage[] spriteNORMAL = new BufferedImage[3]; // sprites when phase = 0
	private BufferedImage[] spriteFOCUS = new BufferedImage[3]; // sprites when phase = 1 or unit has been selected
	private BufferedImage[] spriteINACTIVE = new BufferedImage[3]; // sprites when phase = 2
	
	private BufferedImage[] walkLEFT = new BufferedImage[3]; // intended to be used for animating units moving from one location to another
	private BufferedImage[] walkUP = new BufferedImage[3];
	private BufferedImage[] walkDOWN = new BufferedImage[3];
	private BufferedImage[] walkRIGHT = new BufferedImage[3];

	public Unit(String fclass,Terrain nmap,int nteam){ // constructor; takes in name of unit to be built, Terrain object and team number
		try{
			Scanner statsFile = new Scanner(new BufferedReader(new FileReader("Stats.txt"))); // opens stats file and reads through it
			while (statsFile.hasNext()){ // token-based; reads each object separated by whitespace
				String tmp=statsFile.next();
				if(fclass.equals(tmp)){ // if name of unit found
					name=tmp;
					hp=Integer.parseInt(statsFile.next()); // grabs stats
					maxhp=hp;
					att=Integer.parseInt(statsFile.next());
					skl=Integer.parseInt(statsFile.next());
					spd=Integer.parseInt(statsFile.next());		
					def=Integer.parseInt(statsFile.next());
					con=Integer.parseInt(statsFile.next());
					mov=Integer.parseInt(statsFile.next());
					attrange=Integer.parseInt(statsFile.next());
					luc=Integer.parseInt(statsFile.next());
					phase=0; // initial condition of unit
					map=nmap;
					team=nteam;
					statsFile.close();
					break; // stop looking
				}
				else{
					statsFile.nextLine();	
				}
			}
		}
		catch(IOException ex){
			System.out.println("Where is the file?");
		}
		for (int i=0;i<3;i++){ // takes in sprites for the unit
			try{
				spriteNORMAL[i] = ImageIO.read(new File("Sprites/Overworld/" + name + "/" + nteam + "/Normal/" + name.toLowerCase() + nteam + (i+1) + ".png"));
				spriteFOCUS[i] = ImageIO.read(new File("Sprites/Overworld/" + name + "/" + nteam + "/Focus/" + name.toLowerCase() + nteam + (i+4) + ".png"));
				spriteINACTIVE[i] = ImageIO.read(new File("Sprites/Overworld/" + name + "/" + nteam + "/Inactive/" + name.toLowerCase() + nteam + (i+1) + ".png"));
				
				walkLEFT[i] = ImageIO.read(new File("Sprites/Overworld/" + name + "/" + nteam + "/Walk/" + name.toLowerCase() + nteam + (i+7) + ".png"));
				walkUP[i] = ImageIO.read(new File("Sprites/Overworld/" + name + "/" + nteam + "/Walk/" + name.toLowerCase() + nteam + (i+10) + ".png"));
				walkDOWN[i] = ImageIO.read(new File("Sprites/Overworld/" + name + "/" + nteam + "/Walk/" + name.toLowerCase() + nteam + (i+13) + ".png"));
				walkRIGHT[i] = ImageIO.read(new File("Sprites/Overworld/" + name + "/" + nteam + "/Walk/" + name.toLowerCase() + nteam + (i+16) + ".png"));
				
			}
			catch (IOException ex){
				System.out.println("Sprites not found!");
			}
			
		}								
	}
	public void displayStats(){ // displays unit's stats in the output
		System.out.println(name+" Hp: "+hp+" Attack: "+att+" Skill: "+skl+" Speed: "+spd+" Defense: "+def+" Constitution: "
			+con+" Movement: "+mov+" Luck: "+luc);
	}
	
	public boolean attack(Unit enemy,int x,int y){ // used for battles between units; takes in another Unit object and its x and y coordinates
		if (willHit(enemy,x,y)){		
			int damage;		
	/////////////////////////////////////////////////////Effective attacks////////////////////////////////////////
			if(name.equals("Swordsman")&&enemy.name.equals("Berserker")){
				damage=(att*2-(enemy.def+enemy.map.getDef(x,y)));
			}
			else if(name.equals("Berserker")&&enemy.name.equals("Halberdier")){
				damage=(att*2-(enemy.def+enemy.map.getDef(x,y)));
			}
			else if(name.equals("Halberdier")&&enemy.name.equals("Swordsman")){
				damage=(att*2-(enemy.def+enemy.map.getDef(x,y)));
			}
	/////////////////////////////////////////////////////Ineffective attacks///////////////////////////////////////
			else if(name.equals("Berserker")&&enemy.name.equals("Swordsman")){
				damage=(att/2-(enemy.def+enemy.map.getDef(x,y)));
			}
			else if(name.equals("Halberdier")&&enemy.name.equals("Berserker")){
				damage=(att/2-(enemy.def+enemy.map.getDef(x,y)));
			}
			else if(name.equals("Swordsman")&&enemy.name.equals("Halberdier")){
				damage=(att/2-(enemy.def+enemy.map.getDef(x,y)));
			}
			else{
				damage=(att-(enemy.def+enemy.map.getDef(x,y)));
			}
			if (damage<1){
				damage=1;
			}
	////////////////////////////////////////////Double Attack///////////////////////////////////////
			if(spd>=(enemy.spd+3)){
				if (willCrit()){
					enemy.hp-=damage*3;
					System.out.println("Critical! "+name+" did "+damage*3+" damage to "+enemy.name);					
					return checkAlive(enemy);
				}
				else{
					enemy.hp-=damage;
					System.out.println(name+" did "+damage+" damage to "+enemy.name);
					return checkAlive(enemy);
				}				
			}
			if(willCrit()){ // critical hits
				enemy.hp-=damage*3;
				System.out.println("Critical! "+name+" did "+damage*3+" damage to "+enemy.name);
				return checkAlive(enemy);
			}
			else{
				enemy.hp-=damage;
				System.out.println(name+" did "+damage+" damage to "+enemy.name);
				return checkAlive(enemy);
				
			}			
		}
		else{ // attack missed
			System.out.println(name+" missed!");
			return true;
		}		
	}
	public boolean willHit(Unit enemy,int x,int y){ // checks if unit's attack will actually land
		int accuracy; //chance of attacking
		accuracy=100+(skl+luc)-(enemy.spd+enemy.luc+enemy.map.getAvoid(x,y));
		if(name.equals("Swordsman")&&enemy.name.equals("Berserker")){
				accuracy+=10;
			}
			else if(name.equals("Berserker")&&enemy.name.equals("Halberdier")){
				accuracy+=10;
			}
			else if(name.equals("Halberdier")&&enemy.name.equals("Swordsman")){
				accuracy+=10;
			}
	/////////////////////////////////////////////////////Ineffective attacks///////////////////////////////////////
			else if(name.equals("Berserker")&&enemy.name.equals("Swordsman")){
				accuracy-=10;
			}
			else if(name.equals("Halberdier")&&enemy.name.equals("Berserker")){
				accuracy-=10;
			}
			else if(name.equals("Swordsman")&&enemy.name.equals("Halberdier")){
				accuracy-=10;
			}
		Random percent=new Random(); // random number generator; if accuracy is bigger, it succeeds; else, it doesn't
		if(percent.nextInt(100)<=accuracy){
			return true;
		}
		else{
			return false;
		}
	}
	public boolean willCrit(){ // checks if an attack will be a critical hit
		int critchance=skl+luc*2;
		Random percent=new Random();
		if(percent.nextInt(100)<=critchance){
			return true;
		}
		else{
			return false;
		}
	}
	public void heal(Unit ally){ // heals a friendly unit; used for clerics
		ally.hp+=att;
		if(ally.hp>ally.maxhp){
			ally.hp=ally.maxhp;
		}
		System.out.println(name+" healed "+ally.name+" "+att+" HP!");
	}
	public int getMov(){ // methods used to obtain stats within a unit
		return mov;
	}
	public int getTeam(){
		return team;
	}
	public String getName(){
		return name;
	}
	public int getHP(){
		return hp;
	}
	public int getMaxHP(){
		return maxhp;
	}
	public int getPhase(){
		return phase;
	}
	public int getAttackRange(){
		return attrange;
	}
	
	public void setPhase(int i){ // changes a unit's phase
		phase=i;
	}
	
	public boolean checkAlive(Unit u){ // sees if a unit still has enough HP
		if (u.hp<1){
			return false;
		}
		else{
			return true;
		}
	}
	
	public BufferedImage getImage(String type,int no){ // used to give images to Drawer class; takes in which type of image to show, and which frame to show
		if (type.equals("normal")){
			return spriteNORMAL[no];
		}
		else if (type.equals("inactive")){
			return spriteFOCUS[no];
		}
		else if (type.equals("focus")){
			return spriteFOCUS[no];
		}
		else if (type.equals("up")){
			return walkUP[no];
		}
		else if (type.equals("left")){
			return walkLEFT[no];
		}
		else if (type.equals("right")){
			return walkRIGHT[no];
		}
		else if (type.equals("down")){
			return walkDOWN[no];
		}
		else{
			return null;
		}
	}
}