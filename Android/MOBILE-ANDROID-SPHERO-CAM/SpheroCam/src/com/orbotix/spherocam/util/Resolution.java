package com.orbotix.spherocam.util;

import android.content.Context;

/**
 * Utility for adjusting values for screen densities and for finding resolutions.
 *
 * @author Adam Williams
 */
public class Resolution {

	public static float width = 480;
	public static float height = 320;
	
	public static float play_width = 440;
	public static float play_height = 280;
	public static int x_tiles = 11;
	public static int y_tiles = 7;
	
	public static float standard_depth = 160;
	public static float high_depth     = 240;
	public static float low_depth      = 120;
	
	public static int getTileSize()
	{
		return getTileSize(1);
	}
	
	public static int getTileSize(final Context context)
	{
		return getTileSize(getScale(context));
	}
	
	public static int getTileSize(final float scale)
	{
		return (int)(convertToDensity(play_width/x_tiles, scale));
	}
	
	public static int convertDpToHD(int dps)
	{
		return (int)(dps * high_depth/standard_depth);
	}
	public static int convertDpToLD(int dps)
	{
		return (int)(dps * low_depth/standard_depth);
	}
	
	public static boolean isSquare()
	{
		return(play_width/x_tiles == play_height/y_tiles);
	}
	
	public static int getScreenY(int y, float density)
	{
		return (int)(convertToDensity(play_height, density)) - convertToDensity(y, density);
	}
	
	public static int convertToDensity(float dps, Context context)
	{
		return convertToDensity(dps, getScale(context));
	}
	
	public static int convertToDensity(float dps, float scale)
	{
		return (int)(dps * scale + 0.5f);
	}
	
	public static Index getDrawableCoords(Index index, Dim size, Context context)
	{
		return getDrawableCoords(new int[]{index.x, index.y}, new int[]{size.w, size.h}, Resolution.getScale(context));
	}
	
	public static Index getDrawableCoords(Index index, int[] size, float scale)
	{
		return getDrawableCoords(new int[]{index.x, index.y}, size, scale);
	}
	
	public static Index getDrawableCoords(Index index, Dim size, float scale)
	{
		return getDrawableCoords(new int[]{index.x, index.y}, new int[]{size.w, size.h}, scale);
	}
	
	public static Index getDrawableCoords(int[] tile_coords, int[] tile_size, float scale)
	{
		
		int x = tile_coords[0] * Resolution.convertToDensity(Resolution.getTileSize(), scale);
		int y = Resolution.getScreenY((tile_coords[1]+tile_size[1]) * Resolution.getTileSize(), scale);
		
		return new Index(x, y);
	}
	
	public static Dim getDrawableSize(Dim tiles, Context context)
	{
		return getDrawableSize(tiles, getScale(context));
	}
	
	public static Dim getDrawableSize(Dim tiles, float scale)
	{
		final int tile = getTileSize(scale);
		return new Dim(tiles.w * tile, tiles.h * tile);
	}
	
	public static float getScale(Context context)
	{
		return context.getResources().getDisplayMetrics().density;
	}
	
	public static int getXResolution(Context context)
	{
		return context.getResources().getDisplayMetrics().widthPixels;
	}
	
	public static int getYResolution(Context context)
	{
		return context.getResources().getDisplayMetrics().heightPixels;
	}
	
	public static Dim getScreenSize(Context context)
	{
		return new Dim(getXResolution(context), getYResolution(context));
	}
	
	public static int ScreenMode(Context context)
	{
		int x = getXResolution(context);
		if(x > 850)
		{
			return 2;
		}else if(x > 790)
		{
			return 1;
		}
		
		return 0;
	}
}
