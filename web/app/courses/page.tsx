"use client";
import {useEffect,useState} from "react";
import Link from "next/link";

type Course={id:string;title:string;description:string|null;category:string;specialty:string|null;academy:string};
export default function Courses(){
 const [lang,setLang]=useState("sq");
 const [items,setItems]=useState<Course[]>([]);
 const [query,setQuery]=useState("");
 const [academy,setAcademy]=useState("");
 const [category,setCategory]=useState("");
 const [error,setError]=useState("");
 const [loading,setLoading]=useState(true);
 useEffect(()=>{setLang(localStorage.getItem("medlearn-lang")==="en"?"en":"sq")},[]);
 useEffect(()=>{
  const abort=new AbortController();
  const params=new URLSearchParams();
  if(query.trim())params.set("q",query.trim());
  if(academy)params.set("academy",academy);
  if(category.trim())params.set("category",category.trim());
  const base=process.env.NEXT_PUBLIC_API_URL||"http://localhost:8080";
  setLoading(true);setError("");
  fetch(`${base}/api/courses?${params.toString()}`,{headers:{"Accept-Language":lang},signal:abort.signal})
   .then(r=>{if(!r.ok)throw Error(lang==="sq"?"Katalogu nuk është i disponueshëm":"Catalog unavailable");return r.json()})
   .then((data:Course[])=>setItems(data))
   .catch(e=>{if(e.name!=="AbortError")setError(e.message)})
   .finally(()=>{if(!abort.signal.aborted)setLoading(false)});
  return ()=>abort.abort();
 },[lang,query,academy,category]);
 return <main className="max-w-6xl mx-auto p-8">
  <header className="flex justify-between items-center"><Link href="/" className="text-2xl font-bold text-blue-800">✚ MedLearn</Link><select aria-label="Language" value={lang} onChange={e=>{setLang(e.target.value);localStorage.setItem("medlearn-lang",e.target.value)}}><option value="sq">Shqip</option><option value="en">English</option></select></header>
  <h1 className="text-3xl font-bold my-8">{lang==="sq"?"Katalogu i kurseve":"Course catalog"}</h1>
  <section aria-label={lang==="sq"?"Filtrat":"Filters"} className="grid gap-3 md:grid-cols-3 mb-8">
   <input className="border rounded-lg p-3" type="search" value={query} onChange={e=>setQuery(e.target.value)} placeholder={lang==="sq"?"Kërko kurse...":"Search courses..."} aria-label={lang==="sq"?"Kërko":"Search"}/>
   <select className="border rounded-lg p-3" value={academy} onChange={e=>setAcademy(e.target.value)} aria-label={lang==="sq"?"Akademia":"Academy"}><option value="">{lang==="sq"?"Të gjitha akademitë":"All academies"}</option><option value="OPEN">Open Academy</option><option value="PROFESSIONAL">Professional Academy</option></select>
   <input className="border rounded-lg p-3" value={category} onChange={e=>setCategory(e.target.value)} placeholder={lang==="sq"?"Kategoria":"Category"} aria-label={lang==="sq"?"Kategoria":"Category"}/>
  </section>
  {error&&<p role="alert" className="text-red-700 mb-4">{error}</p>}
  {loading?<p role="status">{lang==="sq"?"Duke ngarkuar...":"Loading..."}</p>:items.length===0?<p>{lang==="sq"?"Nuk u gjetën kurse.":"No courses found."}</p>:<div className="grid md:grid-cols-3 gap-5">{items.map(c=><article className="card" key={c.id}><p className="text-teal-700">{c.category} · {c.specialty||""}</p><h2 className="text-xl font-bold my-2">{c.title}</h2><p>{c.description||""}</p><p className="my-3 text-sm">{c.academy}</p><Link className="primary inline-block" href={'/courses/'+c.id}>{lang==="sq"?"Detajet":"Details"}</Link></article>)}</div>}
 </main>;
}
