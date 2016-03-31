import database.DB
import server.App
import server.middleware.Logger

app = new App( { config ->
    config.setServeStatic true
    config.useConnectionPool true
})

app.use new Logger()

app.get '/api/json/:id',  {  req, res ->
    DB db = app.db 'production'
    res.json  db.first('SELECT * FROM municipios WHERE id = ?', req.params.getInt('id'))
}

app.listen 3000