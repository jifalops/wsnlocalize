package com.jifalops.wsnlocalize.toolbox.neuralnet;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 */
public class SampleList extends ArrayList<Sample> {
    private Scaler scaler;

    public SampleList() {}

    public SampleList(int capacity) { super(capacity); }

    public SampleList(List<double[]> samples, final int numOutputs) {
        super(samples.size());
        for (final double[] a : samples) {
            add(new Sample() {
                @Override
                public double[] toArray() {
                    return a;
                }

                @Override
                public int getNumOutputs() {
                    return numOutputs;
                }
            });
        }
    }

    public boolean isValid(Collection<? extends Sample> collection) {
        if (collection.size() == 0) return false;
        int outs = 0, len = 0;
        if (size() > 0) {
            outs = get(0).getNumOutputs();
            len = get(0).toArray().length;
            for (Sample s : collection)
                if (s.getNumOutputs() != outs || s.toArray().length != len) return false;
        } else {
            // internal consistency
            boolean first = true;
            for (Sample s : collection) {
                if (first) {
                    outs = s.getNumOutputs();
                    len = s.toArray().length;
                    first = false;
                }
                if (s.getNumOutputs() != outs || s.toArray().length != len) return false;
            }
        }
        return true;
    }

    public boolean isValid(Sample s) {
        if (size() == 0) return true;
        else return get(0).getNumOutputs() == s.getNumOutputs() &&
                get(0).toArray().length == s.toArray().length;
    }

    @Override
    public void add(int index, Sample object) {
        if (isValid(object)) {
            super.add(index, object);
            scaler = null;
        }
    }

    /** @return true if the sample was added, false if it has the wrong number of outputs. */
    @Override
    public boolean add(Sample s) {
        if (isValid(s)) {
            scaler = null;
            return super.add(s);
        }
        return false;
    }

    /** @return true if all items have the same number of outputs and were added, false if
     * any sample contains the wrong number of outputs (the whole collection is rejected).
     */
    @Override
    public boolean addAll(Collection<? extends Sample> collection) {
        if (isValid(collection)) {
            scaler = null;
            return super.addAll(collection);
        }
        return false;
    }

    @Override
    public boolean addAll(int index, Collection<? extends Sample> collection) {
        if (isValid(collection)) {
            scaler = null;
            return super.addAll(index, collection);
        }
        return false;
    }

    public List<double[]> toDoubleList() {
        List<double[]> list = new ArrayList<>(size());
        for (Sample s : this) {
            list.add(s.toArray());
        }
        return list;
    }

    public double[][] toDoubleArray() {
        return toDoubleList().toArray(new double[size()][]);
    }

    public int getNumOutputs() { return size() > 0 ? get(0).getNumOutputs() : 0; }

    public Scaler getScaler() {
        if (scaler == null && size() > 0) {
            scaler = new Scaler(toDoubleArray(), getNumOutputs());
        }
        return scaler;
    }
}
