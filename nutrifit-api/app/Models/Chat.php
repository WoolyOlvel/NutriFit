<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class Chat extends Model
{
    use HasFactory;

    protected $table = 'chat';

    protected $fillable = [
        'name',
        'message',
        'image',
        'time',
        'read',
        'isOnline',
        'isCurrentUser',
    ];

    // 'Time' debe tratarse como objeto datetime
    protected $casts = [
        'time' => 'datetime',
    ];
}
