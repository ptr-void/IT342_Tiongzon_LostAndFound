"use client";

import { Suspense, useEffect, useRef, useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import Link from "next/link";
import { ArrowLeft, Loader2, UploadCloud, X } from "lucide-react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";

const API_BASE = process.env.NEXT_PUBLIC_API_BASE_URL || "http://localhost:8080/api";

function ClaimForm() {
  const searchParams = useSearchParams();
  const router = useRouter();
  const itemId = searchParams.get("itemId");
  const [claimantId, setClaimantId] = useState<number | null>(null);
  const [itemOwnerId, setItemOwnerId] = useState<number | null>(null);
  const [proofDescription, setProofDescription] = useState("");
  const [proofImageFile, setProofImageFile] = useState<File | null>(null);
  const [proofPreview, setProofPreview] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const fileInputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    const token = localStorage.getItem("token");
    if (!token) return router.push("/login");
    Promise.all([
      fetch(`${API_BASE}/users/me`, { headers: { Authorization: `Bearer ${token}` } }),
      itemId ? fetch(`${API_BASE}/items/${itemId}`) : Promise.resolve(null)
    ])
      .then(async ([userRes, itemRes]) => {
        if (!userRes.ok) throw new Error();
        const user = await userRes.json();
        setClaimantId(user.userId);
        if (itemRes?.ok) {
          const item = await itemRes.json();
          setItemOwnerId(item.reporterId);
        }
      })
      .catch(() => router.push("/login"));
  }, [router, itemId]);

  const selectProofImage = (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (!file) return;
    setProofImageFile(file);
    const reader = new FileReader();
    reader.onloadend = () => setProofPreview(reader.result as string);
    reader.readAsDataURL(file);
  };

  const clearProofImage = () => {
    setProofImageFile(null);
    setProofPreview(null);
    if (fileInputRef.current) fileInputRef.current.value = "";
  };

  const uploadProofImage = async () => {
    if (!proofImageFile) return "";
    const formData = new FormData();
    formData.append("file", proofImageFile);
    formData.append("upload_preset", "lost_and_found_unsigned");
    formData.append("cloud_name", "defkzzqcs");

    const res = await fetch("https://api.cloudinary.com/v1_1/defkzzqcs/image/upload", {
      method: "POST",
      body: formData
    });
    const data = await res.json();
    if (!res.ok || !data.secure_url) throw new Error("Failed to upload proof image.");
    return data.secure_url as string;
  };

  const submit = async (event: React.FormEvent) => {
    event.preventDefault();
    if (!itemId || !claimantId) return;
    setSubmitting(true);
    try {
      const token = localStorage.getItem("token");
      const proofImagePath = await uploadProofImage();
      const res = await fetch(`${API_BASE}/claims`, { method: "POST", headers: { "Content-Type": "application/json", Authorization: `Bearer ${token}` }, body: JSON.stringify({ itemId: Number(itemId), claimantId, proofDescription, proofImagePath }) });
      if (res.ok) {
        toast.success("Claim submitted. The reporter will be notified.");
        router.push("/profile");
      } else {
        toast.error((await res.json().catch(() => ({}))).message || "Failed to submit claim.");
      }
    } catch (error: any) {
      toast.error(error.message || "Failed to submit claim.");
    } finally {
      setSubmitting(false);
    }
  };

  if (!itemId) return <div className="flex h-[50vh] flex-col items-center justify-center gap-3"><p>No item selected.</p></div>;
  if (claimantId && itemOwnerId && claimantId === itemOwnerId) return <div className="flex h-[50vh] flex-col items-center justify-center gap-3 text-center"><p className="font-semibold">You cannot file a claim on your own item.</p></div>;

  return <Card><CardHeader><CardTitle>File a Claim</CardTitle><CardDescription>Provide ownership proof for item #{itemId}.</CardDescription></CardHeader><CardContent><form onSubmit={submit} className="space-y-5"><div className="space-y-2"><label className="text-sm font-semibold">Proof Description</label><textarea required value={proofDescription} onChange={(e) => setProofDescription(e.target.value)} placeholder="Describe unique identifiers, contents, serial numbers, or other proof..." className="min-h-32 w-full rounded-md border bg-background px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-rose-500" /></div><div className="space-y-2"><label className="text-sm font-semibold">Proof Image</label><input ref={fileInputRef} type="file" accept="image/*" onChange={selectProofImage} className="hidden" /><div className="rounded-xl border border-dashed border-rose-200 bg-rose-50/50 p-4 dark:border-rose-900/40 dark:bg-rose-950/10">{proofPreview ? <div className="relative overflow-hidden rounded-lg"><img src={proofPreview} alt="Proof preview" className="h-48 w-full object-cover" /><button type="button" onClick={clearProofImage} className="absolute right-2 top-2 rounded-full bg-white/90 p-2 text-rose-800 shadow"><X className="h-4 w-4" /></button></div> : <button type="button" onClick={() => fileInputRef.current?.click()} className="flex w-full flex-col items-center justify-center gap-2 rounded-lg py-8 text-rose-800"><UploadCloud className="h-8 w-8" /><span className="text-sm font-semibold">Upload receipt/photo proof</span><span className="text-xs text-slate-500">PNG, JPG, or WEBP</span></button>}</div></div><Button type="submit" disabled={submitting || !claimantId} className="w-full bg-rose-900 text-white hover:bg-rose-950">{submitting ? <><Loader2 className="h-4 w-4 animate-spin" /> Submitting...</> : "Submit Claim"}</Button></form></CardContent></Card>;
}

export default function NewClaimPage() {
  return <div className="min-h-[calc(100vh-4rem)] bg-slate-50 px-4 py-8 dark:bg-slate-950"><div className="container mx-auto max-w-2xl"><Link href="/items" className="mb-4 inline-flex items-center text-sm text-rose-800"><ArrowLeft className="mr-2 h-4 w-4" /> Back to Catalog</Link><Suspense fallback={<Loader2 className="h-8 w-8 animate-spin" />}><ClaimForm /></Suspense></div></div>;
}
