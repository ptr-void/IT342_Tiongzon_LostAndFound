"use client";

import { useEffect, useRef, useState } from "react";
import L from "leaflet";

const ICON = L.icon({
    iconUrl: "https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png",
    shadowUrl: "https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png",
    iconSize: [25, 41],
    iconAnchor: [12, 41],
});

interface MapPickerProps {
    onLocationSelect: (lat: number, lng: number) => void;
    initialLat?: number;
    initialLng?: number;
}

export default function MapPicker({ onLocationSelect, initialLat, initialLng }: MapPickerProps) {
    const containerRef = useRef<HTMLDivElement>(null);
    const mapRef = useRef<L.Map | null>(null);
    const markerRef = useRef<L.Marker | null>(null);
    const [ready, setReady] = useState(false);

    useEffect(() => {
        setReady(true);
    }, []);

    useEffect(() => {
        if (!ready || !containerRef.current) return;
        if (mapRef.current) return;

        const center: [number, number] = initialLat && initialLng
            ? [initialLat, initialLng]
            : [10.3157, 123.8854];

        const map = L.map(containerRef.current, {
            center,
            zoom: 13,
            scrollWheelZoom: false,
        });

        L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
            attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
        }).addTo(map);

        if (initialLat && initialLng) {
            const m = L.marker([initialLat, initialLng], { icon: ICON }).addTo(map);
            markerRef.current = m;
        }

        map.on("click", (e) => {
            const { lat, lng } = e.latlng;
            if (markerRef.current) {
                markerRef.current.setLatLng([lat, lng]);
            } else {
                markerRef.current = L.marker([lat, lng], { icon: ICON }).addTo(map);
            }
            onLocationSelect(lat, lng);
        });

        mapRef.current = map;

        return () => {
            map.remove();
            mapRef.current = null;
            markerRef.current = null;
        };
    }, [ready]);

    return (
        <div className="h-full w-full relative">
            <div
                ref={containerRef}
                className="h-full w-full rounded-md z-0"
            />
            {!ready && (
                <div className="absolute inset-0 bg-slate-100 dark:bg-slate-800 rounded-md animate-pulse" />
            )}
        </div>
    );
}
