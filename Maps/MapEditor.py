# MapEditor.py

from pygame import *
import os

screen = display.set_mode((800,600))
display.set_caption("Level Editor - Fire Emblem")

os.environ['SDL_VIDEO_WINDOW_POS'] = '10,20'

chooseMap = "3"

plainTile = image.load("Sprites/Terrain/plain.png")
forestTile = image.load("Sprites/Terrain/forest.png")
mountainTile = image.load("Sprites/Terrain/mountain.png")
roadTile = image.load("Sprites/Terrain/road.png")
riverTile = image.load("Sprites/Terrain/water1.png")
bridge1Tile = image.load("Sprites/Terrain/bridge1.png")
bridge2Tile = image.load("Sprites/Terrain/bridge2.png")

campTile = image.load("Sprites/Terrain/camp.png")
castleTile = image.load("Sprites/Terrain/castle.png")
houseTile = image.load("Sprites/Terrain/house.png")
iShopTile = image.load("Sprites/Terrain/itemShop.png")
wShopTile = image.load("Sprites/Terrain/weaponShop.png")
command1Tile = image.load("Sprites/Terrain/command.png")
command2Tile = image.load("Sprites/Terrain/command2.png")

running = True

def get(grid,x,y):
    '''=========================================================================
    McKenzie's code.
    This function is used to check positions on a grid easily.
    ========================================================================='''
    if 0 <= x <= 19 and 0 <= y <= 14: # returns value in a position if position is valid
        return grid[y][x]
    else: # if position is invalid (out of bounds)
        return -1

##def roadDraw(world,posx,posy):
##    '''=========================================================================
##    This function draws roads on the map.
##    There are different road tiles used depending on
##    the other road tiles surrounding that tile.
##    ========================================================================='''
##    superPos = ((posx*35),(posy*35)) # position to be blitted in
##    if get(world,posx+1,posy) == 1 and get(world,posx,posy+1) == 1:
##        screen.blit(road3Tile,superPos) # right to down
##    elif get(world,posx-1,posy) == 1 and get(world,posx,posy+1) == 1:
##        screen.blit(road4Tile,superPos) # left to down
##    elif get(world,posx+1,posy) == 1 and get(world,posx,posy-1) == 1:
##        screen.blit(road5Tile,superPos) # up to right
##    elif get(world,posx-1,posy) == 1 and get(world,posx,posy-1) == 1:
##        screen.blit(road6Tile,superPos) # up to left
##    elif get(world,posx,posy+1) == 1 or get(world,posx,posy-1) == 1:
##        screen.blit(road1Tile,superPos) # vertical
##    elif get(world,posx,posy) == 1:
##        screen.blit(road2Tile,superPos) # horizontal

def roadDraw(world,posx,posy):
    superpos = ((posx*40),(posy*40))
    if get(world,posx,posy+1) == 2 and get(world,posx,posy-1) == 2:
        screen.blit(bridge1Tile,superpos)
    elif get(world,posx+1,posy) == 2 and get(world,posx-1,posy) == 2:
        screen.blit(bridge2Tile,superpos)
    else:
        screen.blit(roadTile,superpos)
        

##def riverDraw(world,posx,posy):
##    '''=========================================================================
##    This function draws rivers on the map.
##    There are different river tiles used depending on
##    the other river tiles surrounding that tile.
##    ========================================================================='''
##    superPos = ((posx*35),(posy*35)) # position to be blitted in
##    if get(world,posx+1,posy) == 2 and get(world,posx,posy+1) == 2:
##        screen.blit(river3Tile,superPos) # right to down
##    elif get(world,posx-1,posy) == 2 and get(world,posx,posy+1) == 2:
##        screen.blit(river4Tile,superPos) # left to down
##    elif get(world,posx+1,posy) == 2 and get(world,posx,posy-1) == 2:
##        screen.blit(river5Tile,superPos) # up to right
##    elif get(world,posx-1,posy) == 2 and get(world,posx,posy-1) == 2:
##        screen.blit(river6Tile,superPos) # up to left
##    elif get(world,posx,posy+1) == 2 or get(world,posx,posy-1) == 2:
##        screen.blit(river2Tile,superPos) # horizontal
##    elif get(world,posx,posy) == 2:
##        screen.blit(river1Tile,superPos) # vertical

def mapDraw(world):
    '''=========================================================================
    This function draws different terrain tiles on the map.
    The functions roadDraw and riverDraw are used for roads and rivers
    due to these tiles' special properties.
    ========================================================================='''
    screen.fill((0,0,0))
    for x in range(20):
        for y in range(15):
            tilePos = ((x*40),(y*40))
            if world[y][x] == 0: screen.blit(plainTile,tilePos)
            elif world[y][x] == 1: roadDraw(gameMap,x,y)
            elif world[y][x] == 2: screen.blit(riverTile,tilePos)
            elif world[y][x] == 3: screen.blit(forestTile,tilePos)
            elif world[y][x] == 4: screen.blit(mountainTile,tilePos)
            elif world[y][x] == 5: screen.blit(campTile,tilePos)
            elif world[y][x] == 6: screen.blit(castleTile,tilePos)
            elif world[y][x] == 7: screen.blit(houseTile,tilePos)
            elif world[y][x] == 8: screen.blit(iShopTile,tilePos)
            elif world[y][x] == 9: screen.blit(wShopTile,tilePos)
            elif world[y][x] == 10: screen.blit(command1Tile,tilePos)
            elif world[y][x] == 11: screen.blit(command2Tile,tilePos)
    return screen.subsurface(Rect(0,0,800,600)).copy()

mapFile = open("Maps/" + chooseMap + ".fem","r") # opens map file
mapRead = mapFile.read().strip().split("\n") # reads and cleans map file, splits it by lines
mapFile.close()
gameMap = [] # actual map to be used by game
for y in mapRead:
    gameMap.append(map(int,y.split(" "))) # cleans map up and turns map values into integers

while running:
    for e in event.get():
        if e.type == QUIT:
            running = False
        if e.type == MOUSEBUTTONDOWN:
            if e.button == 1:
                mb = mouse.get_pressed()
                mx,my = mouse.get_pos()
                if mx == 700:
                    mx = 699
                if my == 700:
                    my = 699
                check = gameMap[my/40][mx/40]
                if check == 11:
                    gameMap[my/40][mx/40] = 0
                else:
                    gameMap[my/40][mx/40] += 1
    mapDraw(gameMap)
    display.flip()

os.remove("Maps/" + chooseMap + ".fem")
newMap = open("Maps/" + chooseMap + ".fem","w")
for stuff in gameMap:
    temp = []
    for x in stuff:
        x = str(x)
        if len(x) == 1:
            x = "0" + x
        temp.append(x)
    newMap.write(" ".join(temp)+"\n")
newMap.close()

quit()
