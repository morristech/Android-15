package duy.phuong.handnote.Recognizer.MachineLearning;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.Random;

import duy.phuong.handnote.DTO.StandardImage;
import duy.phuong.handnote.Listener.LearningListener;
import duy.phuong.handnote.Recognizer.Recognizer;
import duy.phuong.handnote.Support.SupportUtils;

/**
 * Created by Phuong on 01/03/2016.
 */

/*SOM Network*/
public class PatternLearning extends Recognizer {
    public static final double INITIAL_LEARNING_RATE = 0.5;//the initial learning rate
    private static final double MAP_RADIUS = (SOM.NUMBERS_OF_CLUSTER / (2 * SOM.NUM_OF_ROWS));

    public enum STATE {
        PLAY, PAUSE, STOP;
    }

    private STATE mState;

    public void setState(STATE State) {
        this.mState = State;
    }

    public interface LearningEndListener {
        void endLearning();
    }

    private ArrayList<Input> mSamples;//training set
    private int mEpochs; //learning rate time const (T2)
    private double mNeighborRadius;
    private double mLearningRate;
    private double mNeighbor_Time_Const; //time const (lambda)

    public PatternLearning(ArrayList<StandardImage> samples, int epochs, SOM som) {
        mMap = new SOM(som);
        init(samples, epochs);
    }

    public PatternLearning(ArrayList<StandardImage> samples, int epochs) {
        mMap = new SOM();
        init(samples, epochs);
    }

    private void init(ArrayList<StandardImage> samples, int epochs) {
        mState = STATE.PLAY;
        mEpochs = epochs; //init time const
        if (mEpochs < 0) {
            mEpochs = 0;
        }
        mSamples = new ArrayList<>();
        for (StandardImage standardImage : samples) {
            Input input = normalizeData(standardImage.getBitmap());
            input.mLabel = standardImage.getName();

            mSamples.add(input);
        }

        //init parameters
        mLearningRate = INITIAL_LEARNING_RATE;
        mNeighborRadius = MAP_RADIUS;
        mNeighbor_Time_Const = mEpochs / Math.log(MAP_RADIUS);
    }

    public void learn(final LearningListener learningListener) {
        final ArrayList<Input> inputs = new ArrayList<>();
        AsyncTask<Void, Bundle, Void> asyncTask = new AsyncTask<Void, Bundle, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                for (int i = 0; i < mEpochs; i++) {
                    if (mState == STATE.PLAY) {
                        mMap.resetMapName();
                        Log.d("Epoch", "" + i);
                        inputs.clear();
                        inputs.addAll(mSamples);

                        while (inputs.size() > 0) {
                            //1. grab a random input
                            Random rd = new Random();
                            int position = rd.nextInt(inputs.size());
                            Input input = inputs.remove(position);

                            //2. find best matching unit
                            double min_distance = 1000000000;
                            int win_neuron_position_X = -1;
                            int win_neuron_position_Y = -1;
                            double d;
                            for (int j = 0; j < mMap.getOutputs().length; j++) {
                                d = getDistance(input, mMap.getOutputs()[j][0]);
                                if (min_distance > d) {
                                    min_distance = d;
                                    win_neuron_position_Y = j;
                                    win_neuron_position_X = 0;
                                }
                                d = getDistance(input, mMap.getOutputs()[j][1]);
                                if (min_distance > d) {
                                    min_distance = d;
                                    win_neuron_position_Y = j;
                                    win_neuron_position_X = 1;
                                }
                                d = getDistance(input, mMap.getOutputs()[j][2]);
                                if (min_distance > d) {
                                    min_distance = d;
                                    win_neuron_position_Y = j;
                                    win_neuron_position_X = 2;
                                }
                                d = getDistance(input, mMap.getOutputs()[j][3]);
                                if (min_distance > d) {
                                    min_distance = d;
                                    win_neuron_position_Y = j;
                                    win_neuron_position_X = 3;
                                }
                                d = getDistance(input, mMap.getOutputs()[j][4]);
                                if (min_distance > d) {
                                    min_distance = d;
                                    win_neuron_position_Y = j;
                                    win_neuron_position_X = 4;
                                }
                                d = getDistance(input, mMap.getOutputs()[j][5]);
                                if (min_distance > d) {
                                    min_distance = d;
                                    win_neuron_position_Y = j;
                                    win_neuron_position_X = 5;
                                }
                                d = getDistance(input, mMap.getOutputs()[j][6]);
                                if (min_distance > d) {
                                    min_distance = d;
                                    win_neuron_position_Y = j;
                                    win_neuron_position_X = 6;
                                }
                                d = getDistance(input, mMap.getOutputs()[j][7]);
                                if (min_distance > d) {
                                    min_distance = d;
                                    win_neuron_position_Y = j;
                                    win_neuron_position_X = 7;
                                }
                            }


                            if (win_neuron_position_X < 0 || win_neuron_position_Y < 0) {
                                Log.d("Error", "An error occur");
                                return null;
                            }

                            //3. find the neighbor area
                            long radius = Math.round(mNeighborRadius);
                            long lowerBoundary_X = win_neuron_position_X - radius;
                            if (lowerBoundary_X < 0) {
                                lowerBoundary_X = 0;
                            }
                            long upperBoundary_X = win_neuron_position_X + radius;
                            if (upperBoundary_X > mMap.getOutputs()[0].length - 1) {
                                upperBoundary_X = mMap.getOutputs()[0].length - 1;
                            }
                            long lowerBoundary_Y = win_neuron_position_Y - radius;
                            if (lowerBoundary_Y < 0) {
                                lowerBoundary_Y = 0;
                            }
                            long upperBoundary_Y = win_neuron_position_Y + radius;
                            if (upperBoundary_Y > mMap.getOutputs().length - 1) {
                                upperBoundary_Y = mMap.getOutputs().length - 1;
                            }

                            //4. update weight vector
                            mMap.updateWeightVector(win_neuron_position_X, win_neuron_position_Y, input, mLearningRate, 1);
                            for (long j = lowerBoundary_Y; j <= upperBoundary_Y; j++) {
                                int index_Y = (int) j;
                                for (long k = lowerBoundary_X; k <= upperBoundary_X; k++) {
                                    int index_X = (int) k;
                                    if (index_X != win_neuron_position_X && index_Y != win_neuron_position_Y) {
                                        mMap.updateWeightVector(index_X, index_Y, input, mLearningRate,
                                                neighborInfluence(index_X, index_Y, win_neuron_position_X, win_neuron_position_Y));
                                    }
                                }
                            }
                        }

                        //check converge condition
                        Bundle bundle = new Bundle();
                        bundle.putInt("Epoch", i);
                        publishProgress(bundle);

                        updateLearningRate(i);
                        updateNeighborRadius(i);
                    } else {
                        if (mState == STATE.PAUSE) {
                            i--;
                        } else {
                            return null;
                        }
                    }
                }

                //Labeling
                mMap.resetMapName();
                for (Input input : mSamples) {
                    double min_distance = Double.MAX_VALUE;
                    int win_neuron_position_X = -1;
                    int win_neuron_position_Y = -1;
                    double d;
                    for (int j = 0; j < mMap.getOutputs().length; j++) {
                        d = getDistance(input, mMap.getOutputs()[j][0]);
                        if (min_distance > d) {
                            min_distance = d;
                            win_neuron_position_Y = j;
                            win_neuron_position_X = 0;
                        }
                        d = getDistance(input, mMap.getOutputs()[j][1]);
                        if (min_distance > d) {
                            min_distance = d;
                            win_neuron_position_Y = j;
                            win_neuron_position_X = 1;
                        }
                        d = getDistance(input, mMap.getOutputs()[j][2]);
                        if (min_distance > d) {
                            min_distance = d;
                            win_neuron_position_Y = j;
                            win_neuron_position_X = 2;
                        }
                        d = getDistance(input, mMap.getOutputs()[j][3]);
                        if (min_distance > d) {
                            min_distance = d;
                            win_neuron_position_Y = j;
                            win_neuron_position_X = 3;
                        }
                        d = getDistance(input, mMap.getOutputs()[j][4]);
                        if (min_distance > d) {
                            min_distance = d;
                            win_neuron_position_Y = j;
                            win_neuron_position_X = 4;
                        }
                        d = getDistance(input, mMap.getOutputs()[j][5]);
                        if (min_distance > d) {
                            min_distance = d;
                            win_neuron_position_Y = j;
                            win_neuron_position_X = 5;
                        }
                        d = getDistance(input, mMap.getOutputs()[j][6]);
                        if (min_distance > d) {
                            min_distance = d;
                            win_neuron_position_Y = j;
                            win_neuron_position_X = 6;
                        }
                        d = getDistance(input, mMap.getOutputs()[j][7]);
                        if (min_distance > d) {
                            min_distance = d;
                            win_neuron_position_Y = j;
                            win_neuron_position_X = 7;
                        }
                    }
                    mMap.updateLabelForCluster(win_neuron_position_X, win_neuron_position_Y, input.mLabel);
                }
                return null;
            }

            @Override
            protected void onProgressUpdate(Bundle... values) {
                super.onProgressUpdate(values);
                learningListener.updateEpoch(values[0]);
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                if (mState == STATE.PLAY) {
                    SupportUtils.writeFile(mMap.toString(), "Trained", "SOM.txt");
                    SupportUtils.writeFile(mMap.getMapNames(), "Trained", "MapNames.txt");
                }
                learningListener.finish();
            }
        };

        asyncTask.execute();
    }

    public void learn(final LearningEndListener listener) {
        AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                for (int i = 0; i < mEpochs; i++) {
                    if (mState == STATE.PLAY) {

                        ArrayList<Input> inputs = new ArrayList<>();
                        inputs.addAll(mSamples);

                        Log.d("Epoch", "" + i + " , size: " + inputs.size());

                        while (inputs.size() > 0) {
                            //1. grab a random input
                            Random rd = new Random();
                            int position = rd.nextInt(inputs.size());
                            Input input = inputs.remove(position);

                            //2. find best matching unit
                            double min_distance = Double.MAX_VALUE;
                            int win_neuron_position_X = -1;
                            int win_neuron_position_Y = -1;
                            for (int j = 0; j < mMap.getOutputs().length; j++) {
                                double d = getDistance(input, mMap.getOutputs()[j][0]);
                                if (min_distance > d) {
                                    min_distance = d;
                                    win_neuron_position_Y = j;
                                    win_neuron_position_X = 0;
                                }
                                d = getDistance(input, mMap.getOutputs()[j][1]);
                                if (min_distance > d) {
                                    min_distance = d;
                                    win_neuron_position_Y = j;
                                    win_neuron_position_X = 1;
                                }
                                d = getDistance(input, mMap.getOutputs()[j][2]);
                                if (min_distance > d) {
                                    min_distance = d;
                                    win_neuron_position_Y = j;
                                    win_neuron_position_X = 2;
                                }
                                d = getDistance(input, mMap.getOutputs()[j][3]);
                                if (min_distance > d) {
                                    min_distance = d;
                                    win_neuron_position_Y = j;
                                    win_neuron_position_X = 3;
                                }
                                d = getDistance(input, mMap.getOutputs()[j][4]);
                                if (min_distance > d) {
                                    min_distance = d;
                                    win_neuron_position_Y = j;
                                    win_neuron_position_X = 4;
                                }
                                d = getDistance(input, mMap.getOutputs()[j][5]);
                                if (min_distance > d) {
                                    min_distance = d;
                                    win_neuron_position_Y = j;
                                    win_neuron_position_X = 5;
                                }
                                d = getDistance(input, mMap.getOutputs()[j][6]);
                                if (min_distance > d) {
                                    min_distance = d;
                                    win_neuron_position_Y = j;
                                    win_neuron_position_X = 6;
                                }
                                d = getDistance(input, mMap.getOutputs()[j][7]);
                                if (min_distance > d) {
                                    min_distance = d;
                                    win_neuron_position_Y = j;
                                    win_neuron_position_X = 7;
                                }
                            }

                            //3. find the neighbor area
                            int radius = (int) Math.round(mNeighborRadius);
                            int lowerBoundary_X = win_neuron_position_X - radius;
                            if (lowerBoundary_X < 0) {
                                lowerBoundary_X = 0;
                            }
                            int upperBoundary_X = win_neuron_position_X + radius;
                            if (upperBoundary_X >= mMap.getOutputs()[0].length) {
                                upperBoundary_X = mMap.getOutputs()[0].length - 1;
                            }
                            int lowerBoundary_Y = win_neuron_position_Y - radius;
                            if (lowerBoundary_Y < 0) {
                                lowerBoundary_Y = 0;
                            }
                            int upperBoundary_Y = win_neuron_position_Y + radius;
                            if (upperBoundary_Y >= mMap.getOutputs().length) {
                                upperBoundary_Y = mMap.getOutputs().length - 1;
                            }

                            //4. update weight vector
                            mMap.updateWeightVector(win_neuron_position_X, win_neuron_position_Y, input, mLearningRate, 1);
                            for (int index_Y = lowerBoundary_Y; index_Y <= upperBoundary_Y; index_Y++) {
                                for (int index_X = lowerBoundary_X; index_X <= upperBoundary_X; index_X++) {
                                    if (index_X != win_neuron_position_X && index_Y != win_neuron_position_Y) {
                                        mMap.updateWeightVector(index_X, index_Y, input, mLearningRate,
                                                neighborInfluence(index_X, index_Y, win_neuron_position_X, win_neuron_position_Y));
                                    }
                                }
                            }
                        }

                        updateLearningRate(i);
                        updateNeighborRadius(i);
                    } else {
                        if (mState == STATE.PAUSE) {
                            i--;
                        } else {
                            return null;
                        }
                    }
                }

                mMap.resetMapName();
                for (Input input : mSamples) {
                    double min_distance = Double.MAX_VALUE;
                    int win_neuron_position_X = -1;
                    int win_neuron_position_Y = -1;
                    double d;
                    for (int j = 0; j < mMap.getOutputs().length; j++) {
                        d = getDistance(input, mMap.getOutputs()[j][0]);
                        if (min_distance > d) {
                            min_distance = d;
                            win_neuron_position_Y = j;
                            win_neuron_position_X = 0;
                        }
                        d = getDistance(input, mMap.getOutputs()[j][1]);
                        if (min_distance > d) {
                            min_distance = d;
                            win_neuron_position_Y = j;
                            win_neuron_position_X = 1;
                        }
                        d = getDistance(input, mMap.getOutputs()[j][2]);
                        if (min_distance > d) {
                            min_distance = d;
                            win_neuron_position_Y = j;
                            win_neuron_position_X = 2;
                        }
                        d = getDistance(input, mMap.getOutputs()[j][3]);
                        if (min_distance > d) {
                            min_distance = d;
                            win_neuron_position_Y = j;
                            win_neuron_position_X = 3;
                        }
                        d = getDistance(input, mMap.getOutputs()[j][4]);
                        if (min_distance > d) {
                            min_distance = d;
                            win_neuron_position_Y = j;
                            win_neuron_position_X = 4;
                        }
                        d = getDistance(input, mMap.getOutputs()[j][5]);
                        if (min_distance > d) {
                            min_distance = d;
                            win_neuron_position_Y = j;
                            win_neuron_position_X = 5;
                        }
                        d = getDistance(input, mMap.getOutputs()[j][6]);
                        if (min_distance > d) {
                            min_distance = d;
                            win_neuron_position_Y = j;
                            win_neuron_position_X = 6;
                        }
                        d = getDistance(input, mMap.getOutputs()[j][7]);
                        if (min_distance > d) {
                            min_distance = d;
                            win_neuron_position_Y = j;
                            win_neuron_position_X = 7;
                        }
                    }
                    mMap.updateLabelForCluster(win_neuron_position_X, win_neuron_position_Y, input.mLabel);
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                if (mState == STATE.PLAY) {
                    SupportUtils.writeFile(mMap.toString(), "Trained", "SOM.txt");
                    SupportUtils.writeFile(mMap.getMapNames(), "Trained", "MapNames.txt");
                }
                listener.endLearning();
            }
        };
        asyncTask.execute();
    }

    private void updateLearningRate(int iteration) {
        mLearningRate = INITIAL_LEARNING_RATE * Math.exp((-1.d * iteration) / mEpochs);
    }

    private void updateNeighborRadius(int iteration) {
        mNeighborRadius = MAP_RADIUS * Math.exp((-1.d * iteration) / mNeighbor_Time_Const);
    }

    private double neighborInfluence(int neighborIndex_X, int neighborIndex_Y, int BMU_index_X, int BMU_index_Y) {
        double value = -1 * (Math.pow(neighborIndex_X - BMU_index_X, 2) + Math.pow(neighborIndex_Y - BMU_index_Y, 2));
        value /= (2 * (Math.pow(mNeighborRadius, 2)));
        return Math.exp(value);
    }
}
