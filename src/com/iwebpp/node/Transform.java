package com.iwebpp.node;

public abstract class Transform 
extends Duplex {

	private TransformState _transformState;

	public static class TransformState {
		private Transform stream;
		private boolean needTransform;
		private boolean transforming;
		private WriteCB writecb;
		private Object writechunk;
		private String writeencoding;

		public TransformState(Transform stream) {
			this.stream = stream;

			this.needTransform = false;
			this.transforming = false;
			this.writecb = null;
			this.writechunk = null;
		}
		@SuppressWarnings("unused")
		private TransformState(){}

		public void afterTransform(String er, Object data) throws Exception {
			afterTransform(stream, er, data);
		}

		private static void afterTransform(Transform stream, String er, Object data) throws Exception {
			TransformState ts = stream._transformState;
			ts.transforming = false;

			WriteCB cb = ts.writecb;

			if (null==cb) {
				///return stream.emit("error", new Error('no writecb in Transform class'));
				stream.emit("error", "no writecb in Transform class");
				return;
			}

			ts.writechunk = null;
			ts.writecb = null;

			///if (!util.isNullOrUndefined(data))
			if (!Util.isNullOrUndefined(data))
				stream.push(data, null);

			if (cb != null)
				cb.writeDone(er);

			State rs = stream._readableState;
			rs.reading = false;
			if (rs.needReadable || rs.length < rs.highWaterMark) {
				stream._read(rs.highWaterMark);
			}
		}

	}

	protected Transform(NodeContext ctx, Options roptions,
			com.iwebpp.node.Writable2.Options woptions) {
		super(ctx, roptions, woptions);
		// TODO Auto-generated constructor stub

		this._transformState = new TransformState(this);

		// when the writable side finishes, then flush out anything remaining.
		final Transform stream = this;

		// start out asking for a readable event once data is transformed.
		this._readableState.needReadable = true;

		// we have implemented the _read method, and done the other things
		// that Readable wants before the first _read call, so unset the
		// sync guard flag.
		this._readableState.sync = false;

		///this.once("prefinish", function() {
		this.once("prefinish", new Listener() {

			@Override
			public void invoke(Object data) throws Exception {
				/*if (util.isFunction(stream._flush))
					  this._flush(function(er) {
						  done(stream, er);
					  });
				  else
					  done(stream);*/
				stream._flush(new flushCallback(){

					@Override
					public void onFlush(String er) throws Exception {
						done(stream, er);						
					}

				});
			}

		});

	}
	private Transform(){
		super(null, null, null);
	}

	protected boolean push(Object chunk, String encoding) throws Exception {
		this._transformState.needTransform = false;
		return super.push(chunk, encoding);
	}

	public void _write(Object chunk, String encoding, WriteCB cb) throws Exception {
		TransformState ts = this._transformState;
		ts.writecb = cb;
		ts.writechunk = chunk;
		ts.writeencoding = encoding;
		if (!ts.transforming) {
			State rs = this._readableState;
			if (ts.needTransform ||
				rs.needReadable ||
				rs.length < rs.highWaterMark)
				this._read(rs.highWaterMark);
		}
	}

	// Doesn't matter what the args are here.
	// _transform does all the work.
	// That we got here means that the readable side wants more data.
	public void _read(int size) throws Exception {
		final TransformState ts = this._transformState;

		if (null!=ts.writechunk && ts.writecb!=null && !ts.transforming) {
			ts.transforming = true;
			///this._transform(ts.writechunk, ts.writeencoding, ts.afterTransform);
			this._transform(ts.writechunk, ts.writeencoding, new afterTransformCallback(){

				@Override
				public void afterTransform(String error) throws Exception {
					ts.afterTransform(error, null);				
				}

			});
		} else {
			// mark that we need a transform, so that any data that comes in
			// will get processed, now that we've asked for it.
			ts.needTransform = true;
		}
	};


	private static boolean done(Transform stream, String er) throws Exception {
		if (er != null) {
			stream.emit("error", er);
			return false;
		}

		// if there's nothing in the write buffer, then that means
		// that nothing more will ever be provided
		com.iwebpp.node.Writable2.State ws = stream._writableState;
		TransformState ts = stream._transformState;

		if (ws.length!=0)
			throw new Error("calling transform done when ws.length != 0");

		if (ts.transforming)
			throw new Error("calling transform done when still transforming");

		return stream.push(null, null);
	}

	// This is the part where you do stuff!
	// override this function in implementation classes.
	// 'chunk' is an input chunk.
	//
	// Call `push(newChunk)` to pass along transformed output
	// to the readable side.  You may call 'push' zero or more times.
	//
	// Call `cb(err)` when you are done with this chunk.  If you pass
	// an error, then that'll put the hurt on the whole operation.  If you
	// never call cb(), then you'll never get another chunk.
	public static interface afterTransformCallback {
		public void afterTransform(String error) throws Exception;
	}
	public abstract void _transform(final Object chunk, String encoding, afterTransformCallback callback);

	public static interface flushCallback {
		public void onFlush(String error) throws Exception;
	}
	public abstract void _flush(flushCallback callback);

}