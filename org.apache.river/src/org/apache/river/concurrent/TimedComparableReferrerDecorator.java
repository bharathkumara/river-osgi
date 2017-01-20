/*
 * Copyright 2012 Zeus Project Services Pty Ltd.
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

/**
 *
 * @param <T> 
 * @author peter
 */
class TimedComparableReferrerDecorator<T> extends TimedReferrerDecorator<T> 
                                        implements Comparable<Referrer<T>> {
    private static final long serialVersionUID = 1L;
    
    TimedComparableReferrerDecorator(Referrer<T> r){
        super(r);
    }
    
    public int compareTo(Referrer<T> o) {
        T t = null;
        Referrer<T> ref = getReference();
        if (ref instanceof UntouchableReferrer){
            t = ((UntouchableReferrer<T>)ref).lookDontTouch();
        } else {
            t = ref.get();
        }
        T r = null;
        if (o instanceof UntouchableReferrer){
            r = ((UntouchableReferrer<T>)o).lookDontTouch();
        } else {
                o.get();
        }
        if ( t != null && r != null) {
            if ( t instanceof Comparable){
                int c = ((Comparable) t).compareTo(r);
                if (c == 0){ // If they were untouchable, this is a hit.
                    ref.get();
                    o.get();
                }
                return c;
            }
        }
        if ( hashCode() < o.hashCode()) return -1;
        if ( hashCode() == o.hashCode()) return 0;
        return 1;
    }
}
