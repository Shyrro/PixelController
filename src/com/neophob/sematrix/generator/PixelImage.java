package com.neophob.sematrix.generator;

import java.security.InvalidParameterException;
import java.util.Random;

import processing.core.PApplet;

import com.neophob.sematrix.input.Sound;
import com.neophob.sematrix.resize.Resize.ResizeName;

/**
 * Space Invader Generator
 * 
 * @author mvogt
 * 
 */
public class PixelImage extends Generator {

	private static final int PIXELNR = 8;
	private static final int NR_OF_IMAGES = 14;

	private int xDiff,yDiff;

	private int[][] grid = new int[PIXELNR][PIXELNR];
	private int[][] images = new int[15][2];

	private Random rnd = new Random();
	private int frame = 0;

	public PixelImage() throws InvalidParameterException {
		super(GeneratorName.PIXELIMAGE, ResizeName.PIXEL_RESIZE);

		//populate known images
		images[0][0]=0x1537; 
		images[0][1]=0xfbdb;        //neorainbowduino logo
		images[1][0]=0x553d; 
		images[1][1]=0xfb15;        //alien head
		images[2][0]=0x177f; 
		images[2][1]=0xf771;        //cross
		images[3][0]=0x6017; 
		images[3][1]=0xf930;        //skull
		images[4][0]=0x4137; 
		images[4][1]=0xa5b0;        //invader  
		images[5][0]=0x11bf; 
		images[5][1]=0xf731;        //heart  
		images[6][0]=0x51bd; 
		images[6][1]=0xe411;        //invader
		images[7][0]=0x999f; 
		images[7][1]=0xf999;        //pause
		images[8][0]=0x1513; 
		images[8][1]=0xf511;        //pacman
		images[9][0]=0x13DF; 
		images[9][1]=0x7B35;        //ant
		images[10][0]=0x6413; 
		images[10][1]=0x8316;       //big eye alien
		images[11][0]=0x4413; 
		images[11][1]=0xA610;       //skull
		images[12][0]=0xFACA; 
		images[12][1]=0x645D;       //pixelinvaders logo
		images[13][0]=0x52C0; 
		images[13][1]=0x0CE1;       //ninja
/*
gelesen von der mitte!
		1010 ....   -> 0xa  -> 0xacaf  .. ganz rechts (F) ist oben
		1100 ....	-> 0xc
		1010 ....	-> 0xa
		1111 ....	-> 0xf
		
		1101 ....	-> 0xd	->0x45d9
		0101 ....	-> 0x5
		0100 ....	-> 0x4
		0110 ....	-> 0x6
*/	

		xDiff = internalBufferXSize/PIXELNR;
		yDiff = internalBufferYSize/PIXELNR;
	}

	@Override
	public void update() {		
		if (frame==0 || Sound.getInstance().getVolumeNormalized()>0.5f && frame>8 || 
				Sound.getInstance().isKick() && frame>40) {
			frame=1;
			doInvader();
			int xofs, yofs=-1, dst=0;

			//resize image from 8x8 to 128x128
			for (int y=0; y<internalBufferXSize; y++) {
				if (y%yDiff==0) yofs++;
				xofs=-1;
				for (int x=0; x<internalBufferXSize; x++) {
					if (x%xDiff==0) xofs++;
					int col = 255*this.grid[xofs][yofs];
					this.internalBuffer[dst++] = (col << 16) | (col << 8)  | col;
				}				
			}
		}
		frame++;
	}

	@Override
	public void close() {	}

	/**
	 * 
	 */
	private void doInvader() {
		int r = rnd.nextInt(7);
		
		switch (r) {
		case 0: //mix prestored
		case 5:  
			invader(images[rnd.nextInt(NR_OF_IMAGES)][0], images[rnd.nextInt(NR_OF_IMAGES)][1]);
			break;
		case 1: //prestored
			int i = rnd.nextInt(NR_OF_IMAGES);
			invader(images[i][0], images[i][1]);
			break;
		case 2: //mutate invader
		case 6:  
			invader(images[rnd.nextInt(NR_OF_IMAGES)][0], images[rnd.nextInt(NR_OF_IMAGES)][1]);
			mutateInvader();
			break;
		case 3: //prestored + random
			invader(images[rnd.nextInt(NR_OF_IMAGES)][0], rnd.nextInt(0xffff));
			break;
		case 4: //random + prestored 
			invader(rnd.nextInt(0xffff), images[rnd.nextInt(NR_OF_IMAGES)][0]);
			break;
		}
	}
	/**
	 * 
	 * @param grid
	 */
	private void mirrorInvader(int[][] grid) {
		for (int y=0; y<8; y++) {
			grid[7][y] = grid[0][y];
			grid[6][y] = grid[1][y];  
			grid[5][y] = grid[2][y]; 
			grid[4][y] = grid[3][y];
		}
	}

	
	/**
	 * create a random mutation
	 */
	private void mutateInvader() {
		for (int y=1; y<7; y++) { // i = columns
			for (int x=0; x<4; x++) { // j = rows
				if (1==rnd.nextInt(3)) {
					grid[x][y] = rnd.nextInt(2);
				}
			}
		}
		mirrorInvader(grid);
	}


	/**
	 * draw an invader
	 * 
	 * @param nr1
	 * @param nr2
	 */
	private void invader(int nr1, int nr2) {
		//sabity checks
		if (nr1>0xffff) {
			nr1=0xffff;
		}
		if (nr2>0xffff) {
			nr2=0xffff;
		}

		//init stuff
		int[] value = new int[4*8];
		int ofs=0;

		int nr=nr1;
		for (int i=0; i<4; i++) {
			if (i==2) {
				nr=nr2;
			}
			String bin = PApplet.binary((int)(nr & 0xff), 8);
			nr = nr>>8;
			for (int j=7; j>-1; j--) {
				char x = bin.charAt(j);
				if (x=='0') {
					value[ofs++] = 0;
				} else {
					value[ofs++] = 1;
				}
			}
		}

		ofs=0;
		for (int y=0; y<8; y++) { // i = columns
			for (int x=0; x<4; x++) { // j = rows
				grid[x][y] = value[ofs++];
			}
		}
		mirrorInvader(grid);
	}
}
