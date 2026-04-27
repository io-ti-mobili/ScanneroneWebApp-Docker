export interface UserProfileDto {
    username: string;
    avatar: string;
    joinDate: string;
    location: string;
    score: string;
    globalRank: number;
    reti: {
        totali: number;
        uniche: number;
        accuracy: string;
        indirizzoCompleto: string;
    };
    geografia: {
        paesi: number;
        regioni: number;
        citta: number;
        nuoveCitta: number;
    }
}
