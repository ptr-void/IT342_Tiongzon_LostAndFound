"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { UserPlus } from "lucide-react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import {
  Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle,
} from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { registerUser } from "@/features/auth/api";

export function RegisterForm() {
  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const router = useRouter();

  const handleRegister = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    try {
      const data = await registerUser({ username, email, password });
      toast.success(data.message || "Account created! You can now log in.");
      setTimeout(() => router.push("/login"), 1500);
    } catch (error: any) {
      toast.error(error.message || "Registration failed.");
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="flex min-h-[calc(100vh-4rem)] items-center justify-center bg-slate-50 font-sans dark:bg-slate-950 px-4 relative overflow-hidden">
      <div className="absolute inset-0 z-0 bg-gradient-to-bl from-indigo-100/60 via-transparent to-transparent dark:from-indigo-900/40" />
      <Card className="w-full max-w-md shadow-xl border-slate-200 dark:border-slate-800 z-10 relative bg-white/80 dark:bg-slate-900/80 backdrop-blur-md">
        <form onSubmit={handleRegister} className="space-y-2">
          <CardHeader className="text-center pb-4">
            <div className="mx-auto mb-4 bg-indigo-100 dark:bg-indigo-900/50 p-3 rounded-full w-fit">
              <UserPlus className="w-6 h-6 text-indigo-600 dark:text-indigo-400" />
            </div>
            <CardTitle className="text-2xl font-bold">Create an Account</CardTitle>
            <CardDescription>Enter your credentials below to get started.</CardDescription>
          </CardHeader>
          <CardContent className="space-y-2">
            <div className="flex flex-col space-y-1.5">
              <Label htmlFor="username">Username</Label>
              <Input id="username" placeholder="Type your username" value={username}
                onChange={(e) => setUsername(e.target.value)} required />
            </div>
            <div className="flex flex-col space-y-1.5">
              <Label htmlFor="email">Email</Label>
              <Input id="email" type="email" placeholder="Type your email" value={email}
                onChange={(e) => setEmail(e.target.value)} required />
            </div>
            <div className="flex flex-col space-y-1.5">
              <Label htmlFor="password">Password</Label>
              <Input id="password" type="password" placeholder="Type your password" value={password}
                onChange={(e) => setPassword(e.target.value)} required />
            </div>
          </CardContent>
          <CardFooter className="flex flex-col gap-4 mt-6">
            <Button type="submit" className="w-full bg-indigo-600 hover:bg-indigo-700 text-white" disabled={isLoading}>
              {isLoading ? "Signing up..." : "Create Account"}
            </Button>
            <div className="text-center text-sm text-slate-500">
              Already have an account?{" "}
              <Link href="/login" className="text-indigo-600 hover:underline hover:text-indigo-700 font-medium">
                Sign in instead
              </Link>
            </div>
          </CardFooter>
        </form>
      </Card>
    </div>
  );
}
