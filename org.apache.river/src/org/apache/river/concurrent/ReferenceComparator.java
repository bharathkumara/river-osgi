/* Copyright (c) 2010-2012 Zeus Project Services Pty Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.river.concurrent;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Comparator;

/**
 *
 * @param <T> 
 * @author Peter Firmstone.
 */
class ReferenceComparator<T> extends AbstractReferenceComparator<T> implements  Serializable{
    private static final long serialVersionUID = 1L;
    private Comparator<? super T> comparator;
    ReferenceComparator(Comparator<? super T> comparator){
        if ( comparator == null ) throw new IllegalArgumentException("Null value prohibited");
        this.comparator = comparator;
    }
    
    Comparator<? super T> get(){
        return comparator;
    }
    
    private void readObject(ObjectInputStream in)
        throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        if ( get() == null ) throw new InvalidObjectException("Null value prohibited");
    }
    
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }

}
