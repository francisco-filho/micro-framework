(function() {
    var path = require('path'),
        Builder = require('systemjs-builder')

    var base = '/home/francisco/apps/dev/jetty-framework/src/main/resources/public',
        config = path.join(base, '/systemjs-config.js'),
        builder = new Builder(base, config)

    builder.buildStatic(path.join('app/*.js'), 'outfile.js', {runtime: false, sourceMaps: true, minify: true})
})()