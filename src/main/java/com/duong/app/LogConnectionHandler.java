package com.duong.app;


import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;

@Sharable
public class LogConnectionHandler extends SimpleChannelInboundHandler<Object>{
	private sync_int conccurent= new sync_int(0);
	public class sync_int{
		private int total0=0;
		public sync_int(int a) { this.total0=a;}
		public int get() {return total0;}
		public void set(int a) {this.total0=a;}
	}
	public int get_total() {return conccurent.get();}
	@Override
	public void channelActive(ChannelHandlerContext ctx){
		synchronized (conccurent) {conccurent.set(conccurent.get()+1); System.err.println(conccurent.get());}
	}
	@Override
	public void channelInactive(ChannelHandlerContext ctx){
		synchronized (conccurent) {conccurent.set(conccurent.get()-1); System.err.println(conccurent.get());}
	}
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
		ctx.fireChannelRead(msg);
	}
}
