package example.netty

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.channel.ChannelInitializer
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel

fun main() {
    val parentGroup = NioEventLoopGroup()
    val workerGroup = NioEventLoopGroup()

    try {
        ServerBootstrap()
            .group(parentGroup, workerGroup)
            .channel(NioServerSocketChannel::class.java)
            .childHandler(object : ChannelInitializer<SocketChannel>() {
                override fun initChannel(ch: SocketChannel) {
                    ch.pipeline().addLast(RudeServerHandler())
                }
            })
            .bind(8080).sync()
            .channel().closeFuture().sync()
    } finally {
        parentGroup.shutdownGracefully()
        workerGroup.shutdownGracefully()
    }
}

private class RudeServerHandler : ChannelInboundHandlerAdapter() {

    private val logger = org.slf4j.LoggerFactory.getLogger(this::class.java)

    override fun channelRegistered(ctx: ChannelHandlerContext) {
        logger.info("[REGISTERED] Channel registered: ${ctx.channel()}")
        super.channelRegistered(ctx)
    }

    override fun channelUnregistered(ctx: ChannelHandlerContext) {
        logger.info("[UNREGISTERED] Channel unregistered: ${ctx.channel()}")
        super.channelUnregistered(ctx)
    }

    override fun channelActive(ctx: ChannelHandlerContext) {
        logger.info("[ACTIVE] Channel active (connected): ${ctx.channel()}")
        ctx.close().await()
        logger.info("[ACTIVE] Connection close initiated")
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        logger.info("[INACTIVE] Channel inactive (disconnected): ${ctx.channel()}")
        super.channelInactive(ctx)
    }

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        logger.info("[READ] Data received from client: ${ctx.channel()}")
        logger.info("[READ] Message content: ${msg}")
    }

    override fun channelReadComplete(ctx: ChannelHandlerContext) {
        logger.info("[READ_COMPLETE] Channel read complete: ${ctx.channel()}")
        super.channelReadComplete(ctx)
    }
}
