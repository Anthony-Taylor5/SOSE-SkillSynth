import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { createUser } from "./api";

export default function SignIn() {
  const navigate = useNavigate();
  const [username, setUsername] = useState("");
  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    try {
      await createUser({ username, level: 1 }); // level hardcoded
      navigate("/dashboard"); // redirect after success
    } catch (err) {
      console.error("Signup failed:", err);
      alert("Failed to create user");
    }
  };


  return (
    <div className="relative min-h-screen flex items-center justify-center overflow-visible">
      <section className="flex flex-col justify-center items-center text-center gap-6 mt-8 select-none w-full max-w-2xl px-4">
        {/* Create Profile Box */}
        <div className="w-full max-w-md rounded-[22px] border border-white/12 bg-white/10 backdrop-blur-xl p-8 md:p-10 shadow-[0_20px_60px_rgba(0,0,0,0.35)] relative z-10">
          <div
            aria-hidden
            className="absolute -inset-px rounded-[22px] opacity-70"
            style={{
              background:
                "linear-gradient(180deg, rgba(2,150,255,0.25), rgba(157,45,252,0.25))",
              filter: "blur(20px)",
              zIndex: -1,
            }}
          />

          <h2 className="text-4xl md:text-5xl font-['Orbitron'] tracking-widest text-transparent bg-clip-text bg-gradient-to-r from-cyan-300 to-purple-300 animate-text-glow mb-6">
            Create Your Profile
          </h2>

          <form onSubmit={handleSubmit} className="space-y-5">
            {/* Username */}
            <div>
              <label
                htmlFor="username"
                className="block text-[0.7rem] uppercase tracking-[0.18em] text-cyan-200/80 font-['Rajdhani'] mb-1"
              >
                Username
              </label>
<input
  id="username"
  name="username"
  type="text"
  autoComplete="username"
  placeholder="your name"
  value={username}                  // connects input to state
  onChange={(e) => setUsername(e.target.value)}  // updates state on type
  required
/>

            </div>

            {/* Password */}
            <div>
              <label
                htmlFor="password"
                className="block text-[0.7rem] uppercase tracking-[0.18em] text-cyan-200/80 font-['Rajdhani'] mb-1"
              >
                Password
              </label>
              <input
                id="password"
                name="password"
                type="password"
                autoComplete="current-password"
                placeholder="••••••••"
                className="w-full rounded-xl bg-black/25 text-white placeholder-white/45 border border-white/15 focus:outline-none focus:ring-2 focus:ring-purple-300/70 focus:border-purple-300/70 px-4 py-3 font-['Rajdhani'] tracking-wide shadow-[inset_0_0_0_999px_rgba(255,255,255,0.02)]"
                required
              />
            </div>

            {/* Email */}
            <div>
              <label
                htmlFor="email"
                className="block text-[0.7rem] uppercase tracking-[0.18em] text-cyan-200/80 font-['Rajdhani'] mb-1"
              >
                Email
              </label>
              <input
                id="email"
                name="email"
                type="email"
                autoComplete="email"
                placeholder="example@email.com"
                className="w-full rounded-xl bg-black/25 text-white placeholder-white/45 border border-white/15 focus:outline-none focus:ring-2 focus:ring-purple-300/70 focus:border-purple-300/70 px-4 py-3 font-['Rajdhani'] tracking-wide shadow-[inset_0_0_0_999px_rgba(255,255,255,0.02)]"
                required
              />
            </div>

            {/* Button */}
            <div className="pt-2 text-center">
              <button className="button" type="submit">
                <span className="font-['Orbitron'] tracking-wide uppercase">Create</span>

                <style>{`
                  .button {
                    position: relative;
                    display: inline-block;
                    text-decoration: none;
                    color: #fff;
                    background: linear-gradient(45deg, #6a0dad, #014f86, #6a0dad);
                    padding: 14px 32px;
                    border-radius: 12px;
                    font-size: 1.1em;
                    letter-spacing: 0.5px;
                    font-weight: 600;
                    cursor: pointer;
                    overflow: hidden;
                    transition: transform 0.3s ease, box-shadow 0.3s ease;
                  }
                  .button span { position: relative; z-index: 2; }
                  .button::before {
                    content: "";
                    position: absolute;
                    inset: 1px;
                    background: #1a1a1a;
                    border-radius: 10px;
                    transition: opacity 0.5s ease;
                    z-index: 1;
                  }
                  .button::after {
                    content: "";
                    position: absolute;
                    inset: 0;
                    background: linear-gradient(45deg, #9d2dfc, #0296ff, #9d2dfc);
                    border-radius: 12px;
                    opacity: 0;
                    filter: blur(15px);
                    transition: opacity 0.5s ease;
                    z-index: 0;
                  }
                  .button:hover {
                    transform: translateY(-2px);
                    box-shadow: 0 0 20px rgba(1,79,134,0.4), 0 0 35px rgba(106,13,173,0.3);
                  }
                  .button:hover::before { opacity: 0.7; }
                  .button:hover::after { opacity: 1; }
                  .button:active {
                    transform: scale(0.97);
                    box-shadow: 0 0 10px rgba(1,79,134,0.3);
                  }
                `}</style>
              </button>
            </div>
          </form>
        </div>

        {/* Animations */}
        <style>{`
          @keyframes textGlow {
            0%, 100% {
              text-shadow: 0 0 8px rgba(155,93,229,0.6), 0 0 15px rgba(0,204,255,0.3);
            }
            50% {
              text-shadow: 0 0 20px rgba(0,204,255,0.8), 0 0 30px rgba(155,93,229,0.6);
            }
          }
          .animate-text-glow { animation: textGlow 4s ease-in-out infinite alternate; }

          @keyframes logoFastSpin {
            0% { transform: scale(0.9) rotate(0deg); opacity: 0; }
            35% { transform: scale(1.05) rotate(300deg); opacity: 1; }
            100% { transform: scale(1.0) rotate(360deg); opacity: 1; }
          }
          .animate-logo-fast { animation: logoFastSpin 0.65s cubic-bezier(.2,.8,.2,1) both; }
        `}</style>
      </section>
    </div>
  );
}