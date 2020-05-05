const path = require('path'),
    express = require('express'),
    webpack = require('webpack'),
    devLoader = require('webpack-dev-middleware'),
    hotLoader = require('webpack-hot-middleware'),
    proxy = require('express-http-proxy'),
    server = express();

function argumentValue(argument) {
    const index = process.argv.indexOf(argument);
    if (index >=0  && process.argv.length > index + 1) {
        return process.argv[index + 1];
    }
    return undefined;
}
const port = argumentValue('--port') || 3000;
const configFileName = argumentValue('--config') || "../webpack.config.js";
console.log(`Using webpack config ${configFileName}`);
const webpackConfig = require(configFileName);

const compiler = webpack(webpackConfig);
server.use(devLoader(compiler, {
    noInfo: true, publicPath: webpackConfig.output.publicPath, stats:{ colors: true }
}));
server.use(hotLoader(compiler));
server.use(express.static(path.resolve(__dirname, '../dist')));
server.use('/api', proxy('localhost:8080'));
server.get('/', (req, res) => {
    res.sendFile(path.resolve(__dirname, '../dist', 'index.html'));
});
server.listen(port, () => { console.log(`App is listening on port ${port}`) });

