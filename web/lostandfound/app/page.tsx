"use client";

import Link from "next/link";
import { Button } from "@/components/ui/button";
import { ArrowRight, Search, MapPin, ShieldCheck, Package, PlusCircle } from "lucide-react";
import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";

export default function Home() {
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [user, setUser] = useState<any>(null);
  const router = useRouter();

  useEffect(() => {
    const token = localStorage.getItem("token");
    if (!token || token === "undefined" || token === "null") {
      setIsLoggedIn(false);
      return;
    }
    fetch("http://localhost:8080/api/users/me", {
      headers: { Authorization: `Bearer ${token}` },
    })
      .then((res) => {
        if (res.ok) return res.json();
        localStorage.removeItem("token");
        return null;
      })
      .then((data) => {
        if (data) {
          setIsLoggedIn(true);
          setUser(data);
        }
      })
      .catch(() => {
        setIsLoggedIn(false);
      });
  }, []);

  if (isLoggedIn && user) {
    return (
      <div className="flex flex-col min-h-[calc(100vh-4rem)]">
        <section className="relative w-full py-20 md:py-28 overflow-hidden bg-slate-50 dark:bg-slate-950">
          <div className="absolute inset-0 z-0 bg-gradient-to-br from-rose-50/80 via-white/80 to-amber-50/80 dark:from-rose-950/80 dark:via-slate-950/80 dark:to-yellow-950/80" />
          <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[700px] h-[700px] bg-amber-200/30 dark:bg-amber-800/10 rounded-full blur-3xl z-0" />

          <div className="container relative z-10 mx-auto px-4 md:px-6 flex flex-col items-center text-center">
            <div className="inline-flex items-center rounded-full border px-2.5 py-0.5 text-xs font-semibold border-transparent bg-emerald-100 text-emerald-800 dark:bg-emerald-900/50 dark:text-emerald-300 mb-6">
              Welcome back, {user.username}!
            </div>
            <h1 className="text-4xl md:text-6xl font-extrabold tracking-tight mb-6 text-slate-900 dark:text-white max-w-3xl">
              Ready to{" "}
              <span className="text-transparent bg-clip-text bg-gradient-to-r from-amber-500 to-rose-800">
                find or report
              </span>{" "}
              an item?
            </h1>
            <p className="text-lg text-slate-600 dark:text-slate-300 max-w-xl mb-10">
              Browse the catalog to see recent reports, or submit a new lost or found item right now.
            </p>
            <div className="flex flex-col sm:flex-row gap-4 w-full sm:w-auto">
              <Link href="/items">
                <Button
                  size="lg"
                  className="w-full sm:w-auto rounded-full h-14 px-8 text-base bg-rose-900 hover:bg-rose-950 text-white shadow-lg shadow-rose-200 dark:shadow-none transition-all"
                >
                  <Package className="mr-2 h-5 w-5" /> Browse Catalog
                </Button>
              </Link>
              <Link href="/report">
                <Button
                  size="lg"
                  variant="outline"
                  className="w-full sm:w-auto rounded-full h-14 px-8 text-base border-2 bg-white/80 backdrop-blur-sm hover:bg-white dark:bg-slate-900/80 dark:hover:bg-slate-800 transition-all text-rose-900 dark:text-rose-400 border-rose-200 dark:border-rose-900/50 hover:border-rose-300"
                >
                  <PlusCircle className="mr-2 h-5 w-5" /> Report an Item
                </Button>
              </Link>
            </div>
          </div>
        </section>

        {}
        <section className="w-full py-20 bg-white dark:bg-slate-900">
          <div className="container mx-auto px-4 md:px-6">
            <div className="text-center mb-16">
              <h2 className="text-3xl font-bold tracking-tight sm:text-4xl mb-4">How it Works</h2>
              <p className="text-lg text-slate-600 dark:text-slate-300">Seamless integration for returning your valuables securely.</p>
            </div>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-12 max-w-5xl mx-auto">
              <div className="flex flex-col items-center text-center">
                <div className="h-16 w-16 rounded-2xl bg-amber-100 dark:bg-amber-900/50 flex items-center justify-center mb-6 shadow-sm border border-amber-200 dark:border-amber-800">
                  <Search className="h-8 w-8 text-amber-600 dark:text-amber-400" />
                </div>
                <h3 className="text-xl font-bold mb-3">1. Search &amp; Browse</h3>
                <p className="text-slate-600 dark:text-slate-400">Search the catalog using rich filters to see if someone found your item safely.</p>
              </div>
              <div className="flex flex-col items-center text-center">
                <div className="h-16 w-16 rounded-2xl bg-rose-100 dark:bg-rose-900/50 flex items-center justify-center mb-6 shadow-sm border border-rose-200 dark:border-rose-800">
                  <MapPin className="h-8 w-8 text-rose-800 dark:text-rose-400" />
                </div>
                <h3 className="text-xl font-bold mb-3">2. Pin Locations</h3>
                <p className="text-slate-600 dark:text-slate-400">Reporters pin the exact location on an interactive map, helping you track the path.</p>
              </div>
              <div className="flex flex-col items-center text-center">
                <div className="h-16 w-16 rounded-2xl bg-emerald-100 dark:bg-emerald-900/50 flex items-center justify-center mb-6 shadow-sm border border-emerald-200 dark:border-emerald-800">
                  <ShieldCheck className="h-8 w-8 text-emerald-600 dark:text-emerald-400" />
                </div>
                <h3 className="text-xl font-bold mb-3">3. Secure Claims</h3>
                <p className="text-slate-600 dark:text-slate-400">Submit private proof of ownership. Optionally pay a courier fee to get it shipped.</p>
              </div>
            </div>
          </div>
        </section>
      </div>
    );
  }

  return (
    <div className="flex flex-col min-h-[calc(100vh-4rem)]">
      <section className="relative w-full py-20 md:py-32 lg:py-40 overflow-hidden bg-slate-50 dark:bg-slate-950">
        <div className="absolute inset-0 z-0 bg-gradient-to-br from-rose-50/80 via-white/80 to-amber-50/80 dark:from-rose-950/80 dark:via-slate-950/80 dark:to-yellow-950/80" />
        <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[800px] h-[800px] bg-amber-200/40 dark:bg-amber-800/20 rounded-full blur-3xl z-0" />

        <div className="container relative z-10 mx-auto px-4 md:px-6 flex flex-col items-center text-center">
          <div className="inline-flex items-center rounded-full border px-2.5 py-0.5 text-xs font-semibold transition-colors border-transparent bg-rose-100 text-rose-800 dark:bg-rose-900/50 dark:text-rose-300 mb-6">
            New Community Platform
          </div>
          <h1 className="text-4xl md:text-6xl lg:text-7xl font-extrabold tracking-tight mb-8 text-slate-900 dark:text-white max-w-4xl">
            Lost something? <br className="hidden md:block" />
            <span className="text-transparent bg-clip-text bg-gradient-to-r from-amber-500 to-rose-800">
              Let&apos;s find it together.
            </span>
          </h1>
          <p className="text-lg md:text-xl text-slate-600 dark:text-slate-300 max-w-2xl mb-12">
            The smartest, fastest community-driven platform to report found items, track your lost belongings, and securely claim what&apos;s yours using modern location pinning.
          </p>
          <div className="flex flex-col sm:flex-row gap-4 w-full sm:w-auto">
            <Link href="/login">
              <Button
                size="lg"
                className="w-full sm:w-auto rounded-full h-14 px-8 text-base bg-rose-900 hover:bg-rose-950 text-white shadow-lg shadow-rose-200 dark:shadow-none transition-all"
              >
                Get Started <ArrowRight className="ml-2 h-5 w-5" />
              </Button>
            </Link>
            <Link href="/register">
              <Button
                size="lg"
                variant="outline"
                className="w-full sm:w-auto rounded-full h-14 px-8 text-base border-2 bg-white/80 backdrop-blur-sm hover:bg-white dark:bg-slate-900/80 dark:hover:bg-slate-800 transition-all text-rose-900 dark:text-rose-400 border-rose-200 dark:border-rose-900/50 hover:border-rose-300"
              >
                Create Account
              </Button>
            </Link>
          </div>
        </div>
      </section>

      <section className="w-full py-20 bg-white dark:bg-slate-900">
        <div className="container mx-auto px-4 md:px-6">
          <div className="text-center mb-16">
            <h2 className="text-3xl font-bold tracking-tight sm:text-4xl mb-4">How it Works</h2>
            <p className="text-lg text-slate-600 dark:text-slate-300">Seamless integration for returning your valuables securely.</p>
          </div>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-12 max-w-5xl mx-auto">
            <div className="flex flex-col items-center text-center">
              <div className="h-16 w-16 rounded-2xl bg-amber-100 dark:bg-amber-900/50 flex items-center justify-center mb-6 shadow-sm border border-amber-200 dark:border-amber-800">
                <Search className="h-8 w-8 text-amber-600 dark:text-amber-400" />
              </div>
              <h3 className="text-xl font-bold mb-3">1. Search &amp; Browse</h3>
              <p className="text-slate-600 dark:text-slate-400">Search the catalog using rich filters to see if someone found your item safely.</p>
            </div>
            <div className="flex flex-col items-center text-center">
              <div className="h-16 w-16 rounded-2xl bg-rose-100 dark:bg-rose-900/50 flex items-center justify-center mb-6 shadow-sm border border-rose-200 dark:border-rose-800">
                <MapPin className="h-8 w-8 text-rose-800 dark:text-rose-400" />
              </div>
              <h3 className="text-xl font-bold mb-3">2. Pin Locations</h3>
              <p className="text-slate-600 dark:text-slate-400">Reporters pin the exact location on an interactive map, helping you track the path.</p>
            </div>
            <div className="flex flex-col items-center text-center">
              <div className="h-16 w-16 rounded-2xl bg-emerald-100 dark:bg-emerald-900/50 flex items-center justify-center mb-6 shadow-sm border border-emerald-200 dark:border-emerald-800">
                <ShieldCheck className="h-8 w-8 text-emerald-600 dark:text-emerald-400" />
              </div>
              <h3 className="text-xl font-bold mb-3">3. Secure Claims</h3>
              <p className="text-slate-600 dark:text-slate-400">Submit private proof of ownership. Optionally pay a courier fee to get it shipped.</p>
            </div>
          </div>
        </div>
      </section>
    </div>
  );
}
