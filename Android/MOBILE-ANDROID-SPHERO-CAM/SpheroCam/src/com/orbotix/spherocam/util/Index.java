package com.orbotix.spherocam.util;

import android.graphics.Rect;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Class that contains an x and y index, and can do a number of operations on this coordinate.
 *
 * @author Adam Williams
 */
public class Index implements Serializable, Comparable<Index> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public int x;
	public int y;
	
	public Index(int index)
	{
		this(index, index);
	}
	
	public Index(int x_index, int y_index)
	{
		this.x = x_index;
		this.y = y_index;
	}
	
	public Index(int[] index)
	{
		this(index[0], index[1]);
	}
	
	public Index()
	{
		this(0, 0);
	}
	
	//Copy constructor
	public Index(Index index)
	{
		this(index.x, index.y);
	}
	
	public Index getSum(Index index)
	{
		return this.getSum(index.x, index.y);
	}
	
	public Index getSum(int x_index, int y_index)
	{
		return new Index(this.x + x_index, this.y + y_index);
	}
	
	public Index getSum(int[] index)
	{
		return this.getSum(index[0], index[1]);
	}
	
	public void add(int amount)
	{
		this.add(amount, amount);
	}
	
	public void add(int[] index)
	{
		this.add(index[0], index[1]);
	}
	
	public void add(Index index)
	{
		this.add(index.x, index.y);
	}
	
	public void add(int x, int y)
	{
		this.x += x;
		this.y += y;
	}
	
	public void set(int x_index, int y_index)
	{
		this.x = x_index; this.y = y_index;
	}
	
	public void set(int[] index)
	{
		this.set(index[0], index[1]);
	}
	
	public void set(Index index)
	{
		this.x = index.x; this.y = index.y;
	}
	
	public int[] multiply(int scale)
	{
		int[] index = {(this.x * scale), (this.y * scale)};
		return index;
	}
	
	public int multiply(int[] matrix)
	{
		return ((this.x * matrix[0]) + (this.y * matrix[1]));
	}
	
	public String print()
	{
		return "("+this.x+", "+this.y+")";
	}
	
	/*
	 * getRect---
	 * 
	 * @return An Android API Rect object made from two Index objects which define corner coordinates.
	 */
	public static Rect getRect(Index topleft, Index bottomright)
	{
		return new Rect(topleft.x, topleft.y, bottomright.x, bottomright.y);
	}
	
	/*
	 * setRect--
	 * 
	 * Sets an Android API Rect object to the value of a Rect as defined by two Index objects which 
	 * define corner coordiantes.
	 */
	public static void setRect(Index topleft, Index bottomright, Rect rect)
	{
		rect.left = topleft.x;
		rect.top = topleft.y;
		rect.right = bottomright.x;
		rect.bottom = bottomright.y;
	}
	
	/*
	 * combineIndexLists
	 * 
	 * @return an ArrayList<Index> of Index objects that excludes any duplicates.
	 */
	public static ArrayList<Index> combineIndexLists(ArrayList<Index> a, ArrayList<Index> b)
	{
		for(Index b_i : b)
		{
			boolean merge = true;
			for(Index a_i : a)
			{
				if(a_i.x == b_i.x && a_i.y == b_i.y)
				{
					merge = false;
					break;
				}
			}
			if(merge)
			{
				a.add(new Index(b_i));
			}
		}
		
		return a;
	}
	
	/*
	 * getCircleArea
	 * 
	 * Returns all the Index objects that would exist within a circular area, based upon the radius received.
	 * 
	 * @return ArrayList<Index> of the area of the circle.
	 */
	public static ArrayList<Index> getCircleArea(Index center, int radius)
	{
		ArrayList<Index> points = new ArrayList<Index>();
		points.add(center);
		
		//Return just the center if the radius is 0.
		if(radius == 0)
		{
			return points;
		}
		
		float r = radius;
		float a = radius;
		float o = 0f;
		
		float sine = 0f;
		float cosine = 1f;
		float angle = 0f;
		
		Index end_point = center.getSum(Math.round(a), Math.round(o));
		Index stop_point = center.getSum(Math.round(a), Math.round(o));
		
		do{
			
			
			
			
			
			int o_inc = 0;
			int a_inc = 0;
			
			
			
			if(o >= 0)
			{
				if(a >= 0)
				{
					if(Math.abs(o) < Math.abs(a))
					{
						o_inc = 1;
						
					}else
					{
						a_inc = -1;
					}
				}else
				{
					if(Math.abs(o) > Math.abs(a))
					{
						a_inc = -1;
					}else
					{
						o_inc = -1;
					}
				}
			}else
			{
				if(a <= 0)
				{
					if(Math.abs(o) < Math.abs(a))
					{
						o_inc = -1;
					}else
					{
						a_inc = 1;
					}
					
				}else
				{
					if(Math.abs(o) > Math.abs(a))
					{
						a_inc = 1;
					}else
					{
						o_inc = 1;
					}
				}
			}
			
			if(a_inc == 0)
			{
				o += o_inc;
				sine = o / r;
				sine = (sine > 1)?1:sine;
				sine = (sine < -1)?-1:sine;
				angle = (float)Math.asin(sine);
				cosine = (float)Math.cos(angle);
				a = (a > 0)?
					r * cosine : 
					r * cosine * -1;
			}else
			{
				a += a_inc;
				cosine = a / r;
				cosine = (cosine > 1)?1:cosine;
				cosine = (cosine < -1)?-1:cosine;
				angle = (float)Math.acos(cosine);
				sine = (float)Math.sin(angle);
				o = (o > 0)? 
					r * sine : 
					r * sine * -1;
			}
			
			Index.combineIndexLists(points, Index.getLineSegment(center, end_point));
			end_point = center.getSum(Math.round(a), Math.round(o));
			
		}while(!(end_point.x == stop_point.x && end_point.y == stop_point.y));
		
		return points;
	}
	
	/*
	 * getLineSegment
	 * 
	 * Gets a list of all Index objects that would exist in a line segment from the 
	 * start coordinate to the finish coordinate.
	 * 
	 * @return ArrayList<Index> of Index objects in the line segment.
	 */
	public static ArrayList<Index> getLineSegment(Index start, Index finish)
	{
		ArrayList<Index> segment = new ArrayList<Index>();
		segment.add(start);
		
		//Single index line segment
		if(start.x == finish.x && start.y == finish.y)
		{
			return segment;
		}
		
		Index delta = new Index(Math.abs(finish.x - start.x), Math.abs(finish.y - start.y));
		Index ray = new Index();
		
		if(start.x < finish.x)
		{
			ray.x = 1;
		}else if(start.x > finish.x)
		{
			ray.x = -1;
		}
		
		if(start.y < finish.y)
		{
			ray.y = 1;
		}else if(start.y > finish.y)
		{
			ray.y = -1;
		}
		Index current = new Index(start);
		
		
		//Horizontal or Vertical slopes
		if(delta.x == 0)
		{
			
			for(int i = 0; i<delta.y; i++)
			{
				current.add(0,ray.y);
				segment.add(new Index(current));
			}
			return segment;
		}else if(delta.y == 0)
		{
			for(int i = 0;i<delta.x; i++)
			{
				current.add(ray.x, 0);
				segment.add(new Index(current));
			}
			return segment;
		}
		
		//We should have only complex lines now. There should be no division by 0 exceptions.
		assert (ray.x != 0 && ray.y != 0);
		
		Index ray2 = new Index(ray);
		int num = 0;
		int den = 0;
		int numadd = 0;
		int steps = 0;
		
		if(delta.x > delta.y)
		{
			ray.y = 0;
			ray2.x = 0;
			num = delta.x / 2;
			den = delta.x;
			numadd = delta.y;
			steps = delta.x;
			
		}else
		{
			ray.x = 0;
			ray2.y = 0;
			num = delta.y /2;
			den = delta.y;
			numadd = delta.x;
			steps = delta.y;
		}
		
		for(int i = 0; i<steps; i++)
		{
			num += numadd;
			if(num >= den)
			{
				num -= den;
				current.add(ray2.x, ray2.y);
			}
			current.add(ray.x, ray.y);
			segment.add(new Index(current));
		}
		
		return segment;
	}
	
	/*
	 * getDistance---
	 * 
	 * Returns a double value of the distance of the hypotenuse of one coordinate to the other.
	 */
	public static double getDistance(Index start, Index end)
	{
		return Math.hypot((Math.abs(start.x - end.x)), (Math.abs(start.y - end.y)));
	}
	

	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Index index) {
		if(this.y > index.y)
		{
			return 1;
		}else if(this.y < index.y)
		{
			return -1;
		}else
		{
			if(this.x > index.x)
			{
				return 1;
			}else if(this.x < index.x)
			{
				return -1;
			}
		}
		return 0;
	}
}
