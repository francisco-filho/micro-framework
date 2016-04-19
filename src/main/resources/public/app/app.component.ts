import {Component, OnInit} from 'angular2/core'
import {Http, Response} from 'angular2/http'
import 'rxjs/add/operator/map';

@Component({
    selector: 'my-app',
    template: `
        <h1>My New App</h1>
        <a (click)="get()">Get Data Here</a><br/>
        <pre>{{serverinfo | json}}</pre>`
})
export class AppComponent{

    serverinfo: any = undefined;

    constructor(private _http: Http){}

    get(){
        return this._http.get('http://localhost:3000/pgadmin/serverinfo')
            .map((res: Response) => { return res.json() })
            .subscribe( json => {
                this.serverinfo = json
                console.log(json)
            })

    }
}