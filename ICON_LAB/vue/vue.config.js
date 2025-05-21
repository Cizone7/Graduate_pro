module.exports = {
    devServer: {
        port: 8080 , // 前端vue端口号修改
        proxy: {
            '/api': {
                target: 'http://localhost:5000',  // 后端 Flask 服务地址
                changeOrigin: true,  // 修改源
                pathRewrite: {
                    '^/api': ''  // 重写路径
                }
            },
        }
    },
}