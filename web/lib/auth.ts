import Keycloak from 'keycloak-js';
export const keycloak=new Keycloak({url:process.env.NEXT_PUBLIC_KEYCLOAK_URL||'http://localhost:8081',realm:process.env.NEXT_PUBLIC_KEYCLOAK_REALM||'medlearn',clientId:process.env.NEXT_PUBLIC_KEYCLOAK_CLIENT_ID||'medlearn-web'});
let started:Promise<boolean>|undefined;
export function initialize(){return started??=keycloak.init({onLoad:'check-sso',pkceMethod:'S256',checkLoginIframe:false});}
export async function api(path:string,options:RequestInit={}){await initialize();if(keycloak.authenticated)await keycloak.updateToken(30);return fetch((process.env.NEXT_PUBLIC_API_URL||'http://localhost:8080')+path,{...options,headers:{...options.headers,...(keycloak.token?{Authorization:`Bearer ${keycloak.token}`}:{})}});}
