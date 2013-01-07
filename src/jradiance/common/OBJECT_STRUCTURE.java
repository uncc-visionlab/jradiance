/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jradiance.common;

/**
 *
 * @author arwillis
 */
public abstract class OBJECT_STRUCTURE {
    int[] os = null;
    public int[] getOS() { return os; }
    public void setOS(int[] os) { this.os = os; }
    //OTYPES.octree_function ofunc;
    abstract public int octree_function(Object ... obj);
}
