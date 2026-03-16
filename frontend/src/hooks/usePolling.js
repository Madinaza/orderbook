import { useEffect } from "react";

export default function usePolling(callback, delay) {
  useEffect(() => {
    callback();
    const id = setInterval(callback, delay);
    return () => clearInterval(id);
  }, [callback, delay]);
}