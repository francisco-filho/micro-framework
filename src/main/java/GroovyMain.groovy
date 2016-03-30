import server.App

app = new App({ config ->
    config.useConnectionPool true
})

app.get( '/api/texto/', { req, res ->
    res.json '''{"title": "hello"}'''
})

app.listen 3000
