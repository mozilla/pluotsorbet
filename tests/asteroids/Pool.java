/**
 * Copyright 2001 Jean-Francois Doue
 *
 * This file is part of Asteroid Zone. Asteroid Zone is free software;
 * you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation;
 * either version 2 of the License, or (at your option) any later version.
 * Asteroid Zone is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with Asteroid Zone; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA  02111-1307  USA
 */

package asteroids;

/**
 * Class to implement efficient, pool based collections.
 * The class supports an iterator method for collection traversal,
 * and methods to add and remove elements. The collection size
 * cannot grow beyond the size of the initial pool of objects
 * provided for the collection.
 * @author Jean-Francois Doue
 * @version 1.1, 2001/10/02
 */
public class Pool extends Object {
   /**
    * An object array the pool where objects are actually stored.
    */
    public Object[] pool;
   /**
    * The number of objects currently in the pool.
    */
    public int count;
   /**
    * The index of the current object.
    */
    public int current;

    /**
     * Initializes the collection with a pool of instances.
     */
    public Pool(Object[] pool) {
        this.pool = pool;
    }

    /**
     * Resets the collection iterator.
     */
    public final void reset() {
        current = count - 1;
    }

    /**
     * Returns the next object in the collection.
     */
    public final Object next() {
        if (current >= 0) {
            return pool[current--];
        }
        return null;
    }

    /**
     * Removes the current object from the collection.
     */
    public final void removeCurrent() {
        if (current + 1 < count - 1) {
            Object tmp = pool[current + 1];
            pool[current + 1] = pool[count - 1];
            pool[count - 1] = tmp;
        }
        count--;
    }

    /**
     * Adds a new object to the collection. The
     * returned object must be initialized by the callee.
     * Returns null if the collection capacity has been
     * exceeded.
     */
    public final Object addNewObject() {
        if (count >= pool.length) {
            return null;
        }
        return pool[count++];
    }

    /**
     * Removes all the objects from the collection.
     */
    public final void removeAll() {
        count = 0;
    }

    /**
     * Returns the size of the collection.
     */
    public final int size() {
        return count;
    }
}
