package com.wynprice.minify;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Constants {

	//TODO: -fixes
	//  Fix issue with removal of nested viewers. For some reason it causes a "leak"
	//  Viewer should be rotated horizontally
	//		- Simply rotate before rendering
	//		- Reverse rotate redstone input signals
	//		- Could possibly do any rotation needed (click on a face with an item to rotate it)
	//  Viewer should play sounds, at a higher pitch
	//		- Mixin head at ServerLevel#playSound (the non entity one)
	//		- Call the playSound method again, but in the viewers dimension + position, and with a increased pitch
	//		- Make sure the call is not infinitely recursive
	//	Sync block entities to client
	//		- First, add a list of block entity compound tags to the S2CSendViewerData packet
	//		- Add a nullable block entity compound tag to the S2CUpdateViewerData packet
	//		- Add a list of block entities to the viewer block entity, along with the PalettedContainer.
	//			This is kept as a cache
	//		- work out how to tell when they're updated, and then just re-sync to client
	//		- it might be good enough to just resync when block changes
	//	Render Properly
	//	    - create my own ReadonlyClientLevel, and override the methods for
	//			+ setting the blockstates/blockentities
	//			+ getting the world time (delegate to the client world)
	//			+ getting the biome (delegate to the client world, at the block where the viewer is)
	//			+ ~add more~
	//		- when it comes to rendering, figure out (probably with mixin) a quick and easy
	//		  way to switch out the PalettedContainer<BlockState> in the correct chunk's section.
	//		  This can be done by accessing the Level#getChunkAt, LevelChunk#getSection (ChunkAccess),
	//		  The best way to do this would be to de-final the states field, and add a mixin to get/set it
	//		- The entire chunk should be rendered like this.
	//			+ Look into where the rendering calls go, and see if I can replicate just rendering a certain section.
	//			+ For now, I don't think we need to cache the rendering result, and it can be re-calculated per frame.
	//				~ look into caching it eventually
	//  Tick Block Entities (Only Clientside)
	//		- Tick the minify block entity
	//          + Add a ticker to the MinifyViewerBlockEntity.
	//			+ This ticker will tick the fake world
	//		- Tick the fake world
	//			+ Swap out the PalettedContainer like above, then call LevelClient#tickEntities

	//TODO: -additions
	//		- Data Lines
	//			+ like redstone wire, but can hold any number of boolean signals
	//				~ These boolean signals act like bits, and are called channels.
	//			+ use OR to combine data lines
	//			+ Data lines should be instant.
	//			+ When a data line neighbours is updated:
	//				~ get all of the data lines connected to it
	//				~ get all of the Data Output blocks connected to the data line, and figure out what
	//				  channels should be set, by performing an OR on it.
	//		- Data Input Block
	//			+ Allows you to convert a redstone signal into a output data line
	//			+ When any redstone line is received in the block, the channel is set to 1, otherwise it's 0
	//			+ When used inside a minify viewer chunk, the connected minify viewer will output a data line
	//			+ The top of the block would have the following texture:
	//				[--] [-] X [+] [++]
	//				Where [...] is a square with that symbol inside, and X is the currently set channel (bit)
	//				By right clicking a [] square, you can change the currently set channel (bit)
	//				- and + change the channel by 1
	//				-- and ++ change the channel by 10
	//		- Data Output Block
	//			+ Allows you to convert a data line to a redstone signal. (15 when on, 0 when off)
	//			+ Will have the same channel selection options as above

	public static final String MOD_ID = "minify";
	public static final String MOD_NAME = "Minify";
	public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);
}