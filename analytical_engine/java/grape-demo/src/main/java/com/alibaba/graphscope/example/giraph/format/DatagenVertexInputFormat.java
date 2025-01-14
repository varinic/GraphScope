package com.alibaba.graphscope.example.giraph.format;

import com.google.common.collect.Lists;

import org.apache.giraph.edge.Edge;
import org.apache.giraph.io.formats.TextVertexInputFormat;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

import java.io.IOException;
import java.util.List;

public class DatagenVertexInputFormat
        extends TextVertexInputFormat<LongWritable, DoubleWritable, DoubleWritable> {

    /**
     * The factory method which produces the {@link org.apache.giraph.io.formats.TextVertexInputFormat.TextVertexReader}
     * used by this input format.
     *
     * @param split   the split to be read
     * @param context the information about the task
     * @return the text vertex reader to be used
     */
    @Override
    public TextVertexInputFormat<LongWritable, DoubleWritable, DoubleWritable>.TextVertexReader
            createVertexReader(InputSplit split, TaskAttemptContext context) throws IOException {
        return new P2PVertexReader();
    }

    public class P2PVertexReader extends TextVertexReaderFromEachLineProcessed<String[]> {

        String SEPARATOR = " ";

        /**
         * Cached vertex id for the current line
         */
        private LongWritable id;

        private DoubleWritable value;

        @Override
        protected String[] preprocessLine(Text line) throws IOException {
            String[] tokens = line.toString().split(SEPARATOR);
            id = new LongWritable(Long.parseLong(tokens[0]));
            value = new DoubleWritable(0.0d);
            return tokens;
        }

        @Override
        protected LongWritable getId(String[] tokens) throws IOException {
            return id;
        }

        @Override
        protected DoubleWritable getValue(String[] tokens) throws IOException {
            return value;
        }

        @Override
        protected Iterable<Edge<LongWritable, DoubleWritable>> getEdges(String[] tokens)
                throws IOException {
            List<Edge<LongWritable, DoubleWritable>> edges = Lists.newArrayListWithCapacity(0);
            return edges;
        }
    }
}
