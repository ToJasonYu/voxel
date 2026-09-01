import { useCallback, useRef, useState } from "react";

// The Web Speech API has no official TS DOM types yet -- this is the minimal
// shape of the bits this component actually uses.
interface SpeechRecognitionResult {
  transcript: string;
}
interface SpeechRecognitionResultEvent extends Event {
  results: ArrayLike<ArrayLike<SpeechRecognitionResult>>;
}
interface SpeechRecognitionInstance extends EventTarget {
  continuous: boolean;
  interimResults: boolean;
  lang: string;
  start(): void;
  stop(): void;
  onresult: ((event: SpeechRecognitionResultEvent) => void) | null;
  onerror: ((event: Event) => void) | null;
  onend: (() => void) | null;
}
type SpeechRecognitionConstructor = new () => SpeechRecognitionInstance;

function getSpeechRecognitionConstructor(): SpeechRecognitionConstructor | null {
  // Chrome/Edge still only expose the prefixed name.
  const w = window as unknown as {
    SpeechRecognition?: SpeechRecognitionConstructor;
    webkitSpeechRecognition?: SpeechRecognitionConstructor;
  };
  return w.SpeechRecognition ?? w.webkitSpeechRecognition ?? null;
}

export function MicButton({
  onTranscript,
  disabled,
}: {
  onTranscript: (transcript: string) => void;
  disabled?: boolean;
}) {
  const [listening, setListening] = useState(false);
  const recognitionRef = useRef<SpeechRecognitionInstance | null>(null);
  const supported = getSpeechRecognitionConstructor() !== null;

  const handleClick = useCallback(() => {
    if (listening) {
      recognitionRef.current?.stop();
      return;
    }

    const SpeechRecognitionCtor = getSpeechRecognitionConstructor();
    if (!SpeechRecognitionCtor) return;

    const recognition = new SpeechRecognitionCtor();
    recognition.lang = "en-US";
    recognition.interimResults = false;
    recognition.continuous = false;

    recognition.onresult = (event) => {
      const transcript = event.results[0]?.[0]?.transcript;
      if (transcript) onTranscript(transcript);
    };
    recognition.onerror = () => setListening(false);
    recognition.onend = () => setListening(false);

    recognitionRef.current = recognition;
    recognition.start();
    setListening(true);
  }, [listening, onTranscript]);

  if (!supported) {
    return <p className="mic-unsupported">Speech recognition isn't supported in this browser -- use the text box below instead.</p>;
  }

  return (
    <button type="button" className={listening ? "mic-button listening" : "mic-button"} onClick={handleClick} disabled={disabled}>
      {listening ? "Listening... (click to stop)" : "\u{1F3A4} Speak a command"}
    </button>
  );
}
