package com.orbotix.spherocam.util;

import java.io.Serializable;

/**
 * Class that contains a width and height and can perform a few operations on this dimension.
 *
 * @author Adam Williams
 */
public class Dim implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public int w = 0;
	public int h = 0;

	public Dim()
	{
		
	}
	
	public Dim(int square_size)
	{
		this(square_size, square_size);
	}
	
	public Dim(int[] size)
	{
		this(size[0], size[1]);
	}
	
	public Dim(int w, int h)
	{
		this.w = w;
		this.h = h;
	}
	
	public Dim(Dim d)
	{
		this(d.w, d.h);
	}
	
	public void set(Dim dim)
	{
		this.w = dim.w;
		this.h = dim.h;
	}
	
	public void set(int w, int h)
	{
		this.w = w;
		this.h = h;
	}
	
	public void set(int i)
	{
		this.set(i, i);
	}
	
	public void add(int w, int h)
	{
		this.w += w;
		this.h += h;
	}
	
	public Dim getNegated()
	{
		return new Dim((this.w * -1), (this.h * -1));
	}
	
	public Dim getSum(Dim dim)
	{
		return this.getSum(dim.w, dim.h);
	}
	
	public Dim getSum(int w, int h)
	{
		return new Dim(this.w+w, this.h+h);
	}

	public int getArea()
	{
		return this.w * this.h;
	}
	
	public void divide(int div)
	{
		this.divide(div, div);
	}
	
	public void divide(int w, int h)
	{
		this.w /= w;
		this.h /= h;
	}
}
