"use client";
import {useEffect,useState} from 'react';
import Link from 'next/link';
import {initialize,keycloak,api} from '../../../lib/auth';
import {Locale} from '../../../lib/i18n';
type Summary={totalCourses:number;publishedCourses:number;unpublishedCourses:number};
export default function AdminReports(){
 const [lang,setLang]=useState<Locale>('sq'),[data,setData]=useState<Summary|null>(null),[error,setError]=useState('');
 useEffect(()=>{setLang(localStorage.getItem('medlearn-lang')==='en'?'en':'sq');initialize().then(async ok=>{if(!ok){void keycloak.login();return;}const res=await api('/api/courses/admin/reporting/summary');if(!res.ok)throw Error('HTTP '+res.status);setData(await res.json());}).catch(e=>setError(String(e)));},[]);
 return <main className="min-h-screen bg-slate-50 p-6 md:p-12"><div className="max-w-5xl mx-auto"><header className="flex justify-between items-center"><Link href="/" className="text-2xl font-bold text-blue-700">✚ MedLearn</Link><select aria-label="Language" value={lang} onChange={e=>{const l=e.target.value as Locale;setLang(l);localStorage.setItem('medlearn-lang',l)}}><option value="sq">Shqip</option><option value="en">English</option></select></header><h1 className="text-3xl font-bold mt-10">{lang==='sq'?'Raportet administrative':'Administrator reporting'}</h1>{error&&<p role="alert" className="text-red-700 mt-4">{error}</p>}<div className="grid md:grid-cols-3 gap-4 mt-8">{([{key:'totalCourses',sq:'Gjithsej kurse',en:'Total courses'},{key:'publishedCourses',sq:'Kurse të publikuara',en:'Published courses'},{key:'unpublishedCourses',sq:'Kurse të papublikuara',en:'Unpublished courses'}] as const).map(k=><div className="card" key={k.key}><p className="text-slate-600">{lang==='sq'?k.sq:k.en}</p><p className="text-3xl font-bold text-teal-700 mt-2">{data?.[k.key]??'—'}</p></div>)}</div></div></main>;
}
